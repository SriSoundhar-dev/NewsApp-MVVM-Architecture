package com.sri.soundhar.newsapp_mvvm_architecture.data.model

import com.google.gson.Gson
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins down how Gson maps the newsapi.org payload onto the models, including the places
 * where a well-formed-but-partial payload does not behave the way the Kotlin types promise.
 */
class ModelParsingTest {

    private val gson = Gson()

    @Test
    fun `a full payload deserializes into the response model`() {
        val json = TestDataFactory.topHeadlinesJson(articleCount = 2)

        val response = gson.fromJson(json, TopHeadlinesResponse::class.java)

        assertEquals("ok", response.status)
        assertEquals(2, response.totalResults)
        assertEquals(TestDataFactory.articles(count = 2), response.articles)
    }

    @Test
    fun `the response model falls back to its declared defaults when fields are missing`() {
        // Every TopHeadlinesResponse parameter has a default, so Kotlin synthesises a no-arg
        // constructor and Gson can use it — the defaults survive.
        val response = gson.fromJson("{}", TopHeadlinesResponse::class.java)

        assertEquals("", response.status)
        assertEquals(0, response.totalResults)
        assertEquals(emptyList<Article>(), response.articles)
    }

    @Test
    fun `a source without an id keeps a null id and the given name`() {
        val source = gson.fromJson("""{ "name": "Reuters" }""", Source::class.java)

        assertNull(source.id)
        assertEquals("Reuters", source.name)
    }

    @Test
    fun `an article with missing fields gets nulls rather than its declared defaults`() {
        // Article.source has no default value, so Kotlin does NOT synthesise a no-arg
        // constructor. Gson therefore allocates the instance through Unsafe, which skips the
        // constructor entirely — so the `= ""` defaults on title/description/url/imageUrl
        // never run either, and every absent field lands as null despite the non-null types.
        val article = gson.fromJson("""{ "title": "Only a title" }""", Article::class.java)

        assertEquals("Only a title", article.title)
        assertNull("description should have defaulted to \"\"", article.description)
        assertNull("url should have defaulted to \"\"", article.url)
        assertNull("imageUrl should have defaulted to \"\"", article.imageUrl)
    }

    @Test
    fun `an article without a source parses but leaves source null`() {
        // TopHeadlineAdapter.bind reads article.source.name unconditionally, so a payload
        // shaped like this reaches the UI and throws an NPE at bind time rather than being
        // rejected here.
        val article = gson.fromJson("""{ "title": "No source" }""", Article::class.java)

        assertNull(article.source)

        val npe = runCatching { article.source.name }.exceptionOrNull()
        assertTrue("Expected an NPE from reading source.name, got $npe", npe is NullPointerException)
    }
}
