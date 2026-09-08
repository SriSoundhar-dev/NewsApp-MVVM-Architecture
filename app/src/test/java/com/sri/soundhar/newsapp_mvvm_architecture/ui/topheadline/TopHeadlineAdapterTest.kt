package com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline

import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import com.google.gson.Gson
import com.sri.soundhar.newsapp_mvvm_architecture.R
import com.sri.soundhar.newsapp_mvvm_architecture.data.model.Article
import com.sri.soundhar.newsapp_mvvm_architecture.databinding.TopHeadlineItemLayoutBinding
import com.sri.soundhar.newsapp_mvvm_architecture.util.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TopHeadlineAdapterTest {

    private lateinit var parent: FrameLayout
    private lateinit var adapter: TopHeadlineAdapter

    @Before
    fun setUp() {
        // The item layout inflates AppCompat views, so it needs the app's MaterialComponents
        // theme rather than the bare application context.
        val context = ContextThemeWrapper(
            RuntimeEnvironment.getApplication(),
            R.style.Theme_NewsAppMVVMArchitecture
        )
        parent = FrameLayout(context)
        adapter = TopHeadlineAdapter(ArrayList())
    }

    @Test
    fun `a new adapter has no items`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `getItemCount reflects the articles added`() {
        adapter.addData(TestDataFactory.articles(count = 3))

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `addData appends to the existing articles instead of replacing them`() {
        val articles = TestDataFactory.articles(count = 2)

        adapter.addData(articles)
        adapter.addData(articles)

        // addData only ever calls addAll, and the adapter exposes no way to clear, so the
        // same two articles are now listed twice. Any future refresh or retry path has to
        // add a clear() first, or the whole feed duplicates on screen.
        assertEquals(4, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder inflates the item layout`() {
        val holder = adapter.onCreateViewHolder(parent, 0)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertTrue(itemBinding.root === holder.itemView)
    }

    @Test
    fun `onBindViewHolder fills in the title description and source`() {
        adapter.addData(listOf(TestDataFactory.article(index = 1)))
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertEquals("Headline 1", itemBinding.textViewTitle.text.toString())
        assertEquals("Description 1", itemBinding.textViewDescription.text.toString())
        assertEquals("The Verge", itemBinding.textViewSource.text.toString())
    }

    @Test
    fun `onBindViewHolder binds the article at the requested position`() {
        adapter.addData(TestDataFactory.articles(count = 3))
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 2)

        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertEquals("Headline 3", itemBinding.textViewTitle.text.toString())
    }

    @Test
    fun `binding an article with no source throws`() {
        // The API can return this shape, and Gson parses it happily — see ModelParsingTest.
        // bind() reads article.source.name unconditionally, so the crash lands here, in the
        // UI, rather than at the network boundary where it could be handled.
        val sourceless = Gson().fromJson("""{ "title": "No source" }""", Article::class.java)
        adapter.addData(listOf(sourceless))
        val holder = adapter.onCreateViewHolder(parent, 0)

        val thrown = runCatching { adapter.onBindViewHolder(holder, 0) }.exceptionOrNull()

        assertTrue("Expected an NPE but got $thrown", thrown is NullPointerException)
        // bind() sets the title before it reads source.name, so the half-populated row
        // confirms the crash is the source lookup specifically, not an earlier field.
        val itemBinding = TopHeadlineItemLayoutBinding.bind(holder.itemView)
        assertEquals("No source", itemBinding.textViewTitle.text.toString())
    }

    @Test
    fun `a bound item is clickable but the handler does nothing yet`() {
        adapter.addData(listOf(TestDataFactory.article(index = 1)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        assertTrue(holder.itemView.hasOnClickListeners())
        holder.itemView.performClick()

        // The Custom Tabs launch in TopHeadlineAdapter is commented out, so tapping an
        // article starts nothing. Article.url is parsed and then never used.
        assertNull(shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity)
    }
}
