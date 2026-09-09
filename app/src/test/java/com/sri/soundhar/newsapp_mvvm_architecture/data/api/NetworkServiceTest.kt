package com.sri.soundhar.newsapp_mvvm_architecture.data.api

import com.google.gson.stream.MalformedJsonException
import com.sri.soundhar.newsapp_mvvm_architecture.uitils.AppConstant
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.HttpURLConnection

/**
 * Exercises the Retrofit contract against a local [MockWebServer], wired the same way
 * `ApplicationModule.provideNetworkService` wires the real one.
 */
class NetworkServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var networkService: NetworkService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        networkService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getTopHeadlines parses a successful response`() = runTest {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 2)))

        val response = networkService.getTopHeadlines("us")

        assertEquals("ok", response.status)
        assertEquals(2, response.totalResults)
        assertEquals(TestDataFactory.articles(count = 2), response.articles)
    }

    @Test
    fun `getTopHeadlines maps urlToImage onto imageUrl and ignores undeclared fields`() = runTest {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 1)))

        val article = networkService.getTopHeadlines("us").articles.single()

        assertEquals("Headline 1", article.title)
        assertEquals("Description 1", article.description)
        assertEquals("https://example.com/article/1", article.url)
        // `urlToImage` in the payload, `imageUrl` on the model.
        assertEquals("https://example.com/article/1.png", article.imageUrl)
        assertEquals("The Verge", article.source?.name)
        assertEquals("the-verge", article.source?.id)
    }

    @Test
    fun `getTopHeadlines sends the country query and the api key header`() = runTest {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 0)))

        networkService.getTopHeadlines("in")

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/top-headlines?country=in", request.path)
        assertEquals(AppConstant.API_KEY, request.getHeader("X-Api-Key"))
    }

    @Test
    fun `getTopHeadlines throws HttpException when the api key is rejected`() = runTest {
        server.enqueue(
            jsonResponse(TestDataFactory.API_KEY_ERROR_JSON, code = HttpURLConnection.HTTP_UNAUTHORIZED)
        )

        val thrown = runCatching { networkService.getTopHeadlines("us") }.exceptionOrNull()

        assertTrue("Expected an HttpException but got $thrown", thrown is HttpException)
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, (thrown as HttpException).code())
    }

    @Test
    fun `getTopHeadlines throws HttpException on a server error`() = runTest {
        server.enqueue(jsonResponse("{}", code = HttpURLConnection.HTTP_INTERNAL_ERROR))

        val thrown = runCatching { networkService.getTopHeadlines("us") }.exceptionOrNull()

        assertTrue("Expected an HttpException but got $thrown", thrown is HttpException)
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, (thrown as HttpException).code())
    }

    @Test
    fun `getTopHeadlines throws when the body is not valid json`() = runTest {
        server.enqueue(jsonResponse("{ this is not json"))

        val thrown = runCatching { networkService.getTopHeadlines("us") }.exceptionOrNull()

        // Gson reports this as MalformedJsonException, an IOException subclass — so a
        // corrupt body is indistinguishable from a dropped connection by the time the
        // ViewModel's `catch` sees it.
        assertTrue("Expected a parse failure but got $thrown", thrown is MalformedJsonException)
        assertTrue("MalformedJsonException should surface as an IOException", thrown is IOException)
    }

    private fun jsonResponse(body: String, code: Int = HttpURLConnection.HTTP_OK) =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
