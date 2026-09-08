package com.sri.soundhar.newsapp_mvvm_architecture.util

import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Source
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.TopHeadlinesResponse

/**
 * Builders for the data the tests share.
 *
 * [article] and [articleJson] describe the same article for a given index, so a MockWebServer
 * test can enqueue [topHeadlinesJson] and assert the parsed result equals [articles].
 */
object TestDataFactory {

    fun source(id: String? = "the-verge", name: String = "The Verge") = Source(id = id, name = name)

    fun article(
        index: Int = 1,
        title: String = "Headline $index",
        description: String = "Description $index",
        url: String = "https://example.com/article/$index",
        imageUrl: String = "https://example.com/article/$index.png",
        source: Source = source(),
    ) = Article(
        title = title,
        description = description,
        url = url,
        imageUrl = imageUrl,
        source = source,
    )

    fun articles(count: Int): List<Article> = List(count) { article(index = it + 1) }

    fun topHeadlinesResponse(
        articles: List<Article> = articles(2),
        status: String = "ok",
    ) = TopHeadlinesResponse(
        status = status,
        totalResults = articles.size,
        articles = articles,
    )

    /**
     * A single article as newsapi.org returns it — including the `author`, `publishedAt` and
     * `content` fields the app's [Article] model does not declare.
     */
    fun articleJson(index: Int = 1): String = """
        {
          "source": { "id": "the-verge", "name": "The Verge" },
          "author": "Reporter $index",
          "title": "Headline $index",
          "description": "Description $index",
          "url": "https://example.com/article/$index",
          "urlToImage": "https://example.com/article/$index.png",
          "publishedAt": "2026-09-0${index}T10:00:00Z",
          "content": "Body $index"
        }
    """.trimIndent()

    fun topHeadlinesJson(articleCount: Int = 2, status: String = "ok"): String {
        val articles = (1..articleCount).joinToString(separator = ",") { articleJson(it) }
        return """
            { "status": "$status", "totalResults": $articleCount, "articles": [$articles] }
        """.trimIndent()
    }

    /** The body newsapi.org returns for a rejected key, alongside a 401. */
    const val API_KEY_ERROR_JSON = """
        {
          "status": "error",
          "code": "apiKeyInvalid",
          "message": "Your API key is invalid or incorrect."
        }
    """
}
