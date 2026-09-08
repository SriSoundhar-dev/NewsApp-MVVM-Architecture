package com.sri.soundhar.newsapp_mvvm_architecture.ui.base

import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UiStateTest {

    @Test
    fun `Loading is a single shared instance`() {
        // Declared as `object`, so identity and equality checks both work on it.
        assertSame(UiState.Loading, UiState.Loading)
        assertEquals(UiState.Loading, UiState.Loading)
    }

    @Test
    fun `Success is compared by its data`() {
        val articles = TestDataFactory.articles(count = 2)

        assertEquals(UiState.Success(articles), UiState.Success(articles.toList()))
        assertNotEquals(UiState.Success(articles), UiState.Success(TestDataFactory.articles(1)))
    }

    @Test
    fun `Error is compared by its message`() {
        assertEquals(UiState.Error("boom"), UiState.Error("boom"))
        assertNotEquals(UiState.Error("boom"), UiState.Error("bang"))
    }

    @Test
    fun `a StateFlow conflates a repeated Success instead of re-emitting it`() = runTest {
        val articles = TestDataFactory.articles(count = 2)
        val uiState = MutableStateFlow<UiState<List<Article>>>(UiState.Loading)
        val seen = mutableListOf<UiState<List<Article>>>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) { uiState.toList(seen) }

        uiState.value = UiState.Success(articles)
        uiState.value = UiState.Success(articles.toList())

        collector.cancel()
        // This is why UiState.Success must stay a data class: TopHeadlineAdapter.addData
        // appends rather than replaces, so a second, equal emission would render the same
        // articles twice. Structural equality is what stops that reaching the adapter.
        assertEquals(listOf(UiState.Loading, UiState.Success(articles)), seen)
    }
}
