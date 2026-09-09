package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import android.app.Activity
import android.content.Intent
import android.widget.FrameLayout
import com.google.gson.Gson
import com.sri.soundhar.newsapp_mvvm_architecture.R
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.databinding.TopHeadlineItemLayoutBinding
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TopHeadlineAdapterTest {

    private lateinit var activity: Activity
    private lateinit var parent: FrameLayout
    private lateinit var adapter: TopHeadlineAdapter

    @Before
    fun setUp() {
        // A real Activity, not the application context: rows are inflated from the
        // RecyclerView's context in production, and Custom Tabs' startActivity refuses a
        // non-Activity context unless FLAG_ACTIVITY_NEW_TASK is set. The theme is the app's
        // own because the item layout inflates AppCompat views.
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setTheme(R.style.Theme_NewsAppMVVMArchitecture)
        parent = FrameLayout(activity)
        adapter = TopHeadlineAdapter(ArrayList())
    }

    @Test
    fun `a new adapter has no items`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `getItemCount reflects the articles added`() {
        adapter.setData(TestDataFactory.articles(count = 3))

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `setData replaces the previous articles instead of appending them`() {
        val articles = TestDataFactory.articles(count = 2)

        adapter.setData(articles)
        adapter.setData(articles)

        // The screen re-renders on every successful load — including when it returns to the
        // foreground and repeatOnLifecycle replays the current state — so appending here
        // used to double the visible feed.
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder inflates the item layout`() {
        val holder = adapter.onCreateViewHolder(parent, 0)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertTrue(itemBinding.root === holder.itemView)
    }

    @Test
    fun `onBindViewHolder fills in the title description and source`() {
        adapter.setData(listOf(TestDataFactory.article(index = 1)))
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertEquals("Headline 1", itemBinding.textViewTitle.text.toString())
        assertEquals("Description 1", itemBinding.textViewDescription.text.toString())
        assertEquals("The Verge", itemBinding.textViewSource.text.toString())
    }

    @Test
    fun `onBindViewHolder binds the article at the requested position`() {
        adapter.setData(TestDataFactory.articles(count = 3))
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 2)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertEquals("Headline 3", itemBinding.textViewTitle.text.toString())
    }

    @Test
    fun `binding an article with no source leaves the source blank`() {
        // The API really can return this shape and Gson parses it happily — see
        // ModelParsingTest. It used to NPE here because bind() read source.name directly.
        val sourceless = Gson().fromJson("""{ "title": "No source" }""", Article::class.java)
        adapter.setData(listOf(sourceless))
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertEquals("No source", itemBinding.textViewTitle.text.toString())
        assertEquals("", itemBinding.textViewSource.text.toString())
        assertEquals("", itemBinding.textViewDescription.text.toString())
    }

    @Test
    fun `tapping an item opens the article url`() {
        adapter.setData(listOf(TestDataFactory.article(index = 1)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.performClick()

        val started = shadowOf(activity).nextStartedActivity
        assertNotNull("Expected a Custom Tab to be launched", started)
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals("https://example.com/article/1", started.data.toString())
    }

    @Test
    fun `tapping an item with no url starts nothing`() {
        adapter.setData(listOf(TestDataFactory.article(index = 1, url = "")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.performClick()

        assertNull(shadowOf(activity).nextStartedActivity)
    }
}
