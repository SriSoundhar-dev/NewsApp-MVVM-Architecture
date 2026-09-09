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
    fun `an article with missing fields falls back to its declared defaults`() {
        // Every Article parameter now has a default, so Kotlin synthesises a no-arg
        // constructor and Gson uses it instead of allocating through Unsafe. That is what
        // makes the defaults below actually run — previously they were all skipped.
        val article = gson.fromJson("""{ "title": "Only a title" }""", Article::class.java)

        assertEquals("Only a title", article.title)
        assertEquals("", article.url)
        assertNull(article.description)
        assertNull(article.imageUrl)
        assertNull(article.source)
    }

    @Test
    fun `an article keeps explicit nulls from the payload`() {
        // newsapi.org really does send these as null, rather than omitting them.
        val json = """{ "title": "t", "description": null, "urlToImage": null, "source": null }"""

        val article = gson.fromJson(json, Article::class.java)

        assertNull(article.description)
        assertNull(article.imageUrl)
        assertNull(article.source)
    }
}
