package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sri.soundhar.newsapp_mvvm_architecture.R
import com.sri.soundhar.newsapp_mvvm_architecture.TestNewsApplication
import com.sri.soundhar.newsapp_mvvm_architecture.di.MOCK_SERVER_PORT
import com.sri.soundhar.newsapp_mvvm_architecture.di.TestApplicationComponent
import com.sri.soundhar.newsapp_mvvm_architecture.util.OkHttpIdlingResource
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import com.sri.soundhar.newsapp_mvvm_architecture.util.atPosition
import com.sri.soundhar.newsapp_mvvm_architecture.util.hasItemCount
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * Drives the Top Headlines screen end to end. [TestNewsApplication] has already pointed the
 * Dagger graph at [MOCK_SERVER_PORT], so the only setup here is enqueuing what the server
 * should reply with.
 *
 * Every test launches the Activity itself rather than using an ActivityScenarioRule: a rule
 * starts the Activity — and therefore the request — before `@Before` could enqueue anything.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TopHeadlineActivityTest {

    private lateinit var server: MockWebServer
    private lateinit var idlingResource: OkHttpIdlingResource
    private var scenario: ActivityScenario<TopHeadlineActivity>? = null

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start(MOCK_SERVER_PORT)

        val application = ApplicationProvider.getApplicationContext<TestNewsApplication>()
        val component = application.applicationComponent as TestApplicationComponent
        idlingResource = OkHttpIdlingResource(component.getOkHttpClient())
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
        scenario?.close()
        server.shutdown()
    }

    @Test
    fun showsTheArticlesOnceTheyLoad() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 3)))

        launchScreen()

        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(hasItemCount(3)))
    }

    @Test
    fun showsTheTitleDescriptionAndSourceOfAnArticle() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 3)))

        launchScreen()

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(0))

        // Scoped to row 0: every fixture article shares the source "The Verge", so a bare
        // withText would match each visible row and fail as ambiguous.
        onView(withId(R.id.recyclerView)).check(
            matches(atPosition(0, hasDescendant(withText("Headline 1"))))
        )
        onView(withId(R.id.recyclerView)).check(
            matches(atPosition(0, hasDescendant(withText("Description 1"))))
        )
        onView(withId(R.id.recyclerView)).check(
            matches(atPosition(0, hasDescendant(withText("The Verge"))))
        )
    }

    @Test
    fun scrollsToTheLastArticle() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 10)))

        launchScreen()

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(9))

        onView(withId(R.id.recyclerView)).check(
            matches(atPosition(9, hasDescendant(withText("Headline 10"))))
        )
    }

    @Test
    fun requestsTheUsTopHeadlines() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 1)))

        launchScreen()
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))

        assertEquals("/top-headlines?country=us", server.takeRequest().path)
    }

    @Test
    fun showsTheSpinnerAndNoListWhileTheRequestIsInFlight() {
        server.enqueue(
            jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 2))
                .setBodyDelay(3, TimeUnit.SECONDS)
        )
        // Espresso would otherwise wait for the very response this test wants to catch
        // mid-flight, so read the view state directly instead.
        IdlingRegistry.getInstance().unregister(idlingResource)

        launchScreen().onActivity { activity ->
            assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.progressBar).visibility)
            assertEquals(View.GONE, activity.findViewById<View>(R.id.recyclerView).visibility)
        }
    }

    @Test
    fun showsAnEmptyListWhenThereAreNoArticles() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 0)))

        launchScreen()

        // An empty feed is a success state, so the (empty) list is shown rather than an error.
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(hasItemCount(0)))
    }

    @Test
    fun showsAnErrorAndRetryWhenTheRequestFails() {
        server.enqueue(jsonResponse("{}", code = HttpURLConnection.HTTP_INTERNAL_ERROR))

        launchScreen()

        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.errorGroup)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewError)).check(matches(withText(R.string.error_loading_news)))
        onView(withId(R.id.buttonRetry)).check(matches(isDisplayed()))
    }

    @Test
    fun retryingAfterAFailureLoadsTheArticles() {
        server.enqueue(jsonResponse("{}", code = HttpURLConnection.HTTP_INTERNAL_ERROR))
        launchScreen()
        onView(withId(R.id.buttonRetry)).check(matches(isDisplayed()))

        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 3)))
        onView(withId(R.id.buttonRetry)).perform(click())

        onView(withId(R.id.errorGroup)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(hasItemCount(3)))
    }

    @Test
    fun returningToTheScreenDoesNotDuplicateTheFeed() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 3)))
        val scenario = launchScreen()
        onView(withId(R.id.recyclerView)).check(matches(hasItemCount(3)))

        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // repeatOnLifecycle re-collects on the way back and the StateFlow replays the
        // current Success, so the same articles are rendered a second time. While the
        // adapter appended, this is exactly where the feed doubled.
        onView(withId(R.id.recyclerView)).check(matches(hasItemCount(3)))
    }

    @Test
    fun tappingAnArticleOpensItsUrl() {
        server.enqueue(jsonResponse(TestDataFactory.topHeadlinesJson(articleCount = 3)))
        launchScreen()
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))

        Intents.init()
        try {
            // Stubbed so the Custom Tab is recorded but no browser actually opens.
            intending(anyIntent())
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

            onView(withId(R.id.recyclerView))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            intended(
                allOf(hasAction(Intent.ACTION_VIEW), hasData("https://example.com/article/1"))
            )
        } finally {
            Intents.release()
        }
    }

    private fun launchScreen(): ActivityScenario<TopHeadlineActivity> =
        ActivityScenario.launch(TopHeadlineActivity::class.java).also { scenario = it }

    private fun jsonResponse(body: String, code: Int = HttpURLConnection.HTTP_OK) =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
