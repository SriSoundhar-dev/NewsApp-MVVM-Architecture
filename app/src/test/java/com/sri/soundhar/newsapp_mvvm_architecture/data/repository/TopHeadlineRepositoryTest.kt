package com.sri.soundhar.newsapp_mvvm_architecture.data.repository

import com.sri.soundhar.newsapp_mvvm_architecture.data.api.NetworkService
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

class TopHeadlineRepositoryTest {

    private val networkService: NetworkService = mock()

    private lateinit var repository: TopHeadlineRepository

    @Before
    fun setUp() {
        repository = TopHeadlineRepository(networkService)
    }

    @Test
    fun `getTopHeadlines emits the articles from the response`() = runTest {
        val articles = TestDataFactory.articles(count = 3)
        whenever(networkService.getTopHeadlines(COUNTRY))
            .thenReturn(TestDataFactory.topHeadlinesResponse(articles))

        val emissions = repository.getTopHeadlines(COUNTRY).toList()

        // One emission, carrying exactly the response's articles — the `status` and
        // `totalResults` wrapper fields are dropped by the repository's `map`.
        assertEquals(listOf(articles), emissions)
    }

    @Test
    fun `getTopHeadlines emits an empty list when the response has no articles`() = runTest {
        whenever(networkService.getTopHeadlines(COUNTRY))
            .thenReturn(TestDataFactory.topHeadlinesResponse(articles = emptyList()))

        val emissions = repository.getTopHeadlines(COUNTRY).toList()

        assertEquals(listOf(emptyList<Any>()), emissions)
    }

    @Test
    fun `getTopHeadlines requests headlines for the country it was given`() = runTest {
        whenever(networkService.getTopHeadlines("in"))
            .thenReturn(TestDataFactory.topHeadlinesResponse())

        repository.getTopHeadlines("in").toList()

        verify(networkService).getTopHeadlines("in")
    }

    @Test
    fun `getTopHeadlines propagates the network failure to the collector`() = runTest {
        val failure = IOException("Network unavailable")
        whenever(networkService.getTopHeadlines(COUNTRY)).thenAnswer { throw failure }

        val thrown = runCatching { repository.getTopHeadlines(COUNTRY).toList() }.exceptionOrNull()

        // The repository has no `catch` of its own; the ViewModel is what turns this into
        // a UiState.Error.
        assertSame(failure, thrown)
    }

    @Test
    fun `getTopHeadlines does not touch the network until the flow is collected`() = runTest {
        repository.getTopHeadlines(COUNTRY)

        verifyNoInteractions(networkService)
    }

    private companion object {
        const val COUNTRY = "us"
    }
}
