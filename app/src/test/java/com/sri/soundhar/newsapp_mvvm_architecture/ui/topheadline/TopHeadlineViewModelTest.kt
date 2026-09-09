package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import app.cash.turbine.test
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.data.repository.TopHeadlineRepository
import com.sri.soundhar.newsapp_mvvm_architecture.ui.base.UiState
import com.sri.soundhar.newsapp_mvvm_architecture.util.MainDispatcherRule
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TopHeadlineViewModelTest {

    /**
     * Backs `viewModelScope` with a [kotlinx.coroutines.test.StandardTestDispatcher], so the
     * fetch launched from `init` stays queued until a test advances the clock. `runTest`
     * picks up the same scheduler, so `advanceUntilIdle()` drains it.
     */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: TopHeadlineRepository = mock()

    @Test
    fun `uiState starts in Loading before the fetch runs`() = runTest {
        stubHeadlines(flowOf(TestDataFactory.articles(count = 2)))

        val viewModel = TopHeadlineViewModel(repository)

        assertEquals(UiState.Loading, viewModel.uiState.value)
        verify(repository, never()).getTopHeadlines(any())
    }

    @Test
    fun `uiState turns into Success carrying the fetched articles`() = runTest {
        val articles = TestDataFactory.articles(count = 3)
        stubHeadlines(flowOf(articles))
        val viewModel = TopHeadlineViewModel(repository)

        advanceUntilIdle()

        assertEquals(UiState.Success(articles), viewModel.uiState.value)
    }

    @Test
    fun `uiState turns into Success with an empty list when there are no articles`() = runTest {
        stubHeadlines(flowOf(emptyList()))
        val viewModel = TopHeadlineViewModel(repository)

        advanceUntilIdle()

        // An empty feed is a success, not an error — the screen shows an empty list.
        assertEquals(UiState.Success(emptyList<Article>()), viewModel.uiState.value)
    }

    @Test
    fun `uiState turns into Error when the fetch fails`() = runTest {
        val failure = RuntimeException("Network unavailable")
        stubHeadlines(flow { throw failure })
        val viewModel = TopHeadlineViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Error but was $state", state is UiState.Error)
        // The message keeps the raw `e.toString()` for logging. It is deliberately not what
        // the user sees — TopHeadlineActivity renders fixed copy and a Retry button.
        assertEquals(
            "java.lang.RuntimeException: Network unavailable",
            (state as UiState.Error).message
        )
    }

    @Test
    fun `the fetch asks for the configured country exactly once`() = runTest {
        stubHeadlines(flowOf(TestDataFactory.articles(count = 1)))
        TopHeadlineViewModel(repository)

        advanceUntilIdle()

        // "us" is what AppConstant.COUNTRY ships as; the ViewModel takes no country argument.
        verify(repository, times(1)).getTopHeadlines("us")
    }

    @Test
    fun `uiState settles on Success without emitting again`() = runTest {
        val articles = TestDataFactory.articles(count = 2)
        stubHeadlines(flowOf(articles))
        val viewModel = TopHeadlineViewModel(repository)
        advanceUntilIdle()

        // Subscribing after the fetch: a new collector gets the settled value and nothing
        // more, so returning to the screen does not re-trigger a load.
        viewModel.uiState.test {
            assertEquals(UiState.Success(articles), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `fetchNews shows Loading again and can succeed after a failure`() = runTest {
        val articles = TestDataFactory.articles(count = 2)
        whenever(repository.getTopHeadlines(any()))
            .thenReturn(flow { throw RuntimeException("Network unavailable") })
            // The delay gives the retry a real suspension point. Without one the ViewModel
            // would set Loading and Success back to back, and the conflating StateFlow would
            // only ever hand the collector the second one.
            .thenReturn(flow { delay(100); emit(articles) })
        val viewModel = TopHeadlineViewModel(repository)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem() is UiState.Error)

            viewModel.fetchNews()

            assertEquals(UiState.Loading, awaitItem())
            assertEquals(UiState.Success(articles), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun stubHeadlines(headlines: Flow<List<Article>>) {
        whenever(repository.getTopHeadlines(any())).thenReturn(headlines)
    }
}
