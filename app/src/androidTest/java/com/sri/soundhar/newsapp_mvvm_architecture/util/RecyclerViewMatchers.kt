package com.sri.soundhar.newsapp_mvvm_architecture.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/** Matches a [RecyclerView] whose adapter currently holds [expected] items. */
fun hasItemCount(expected: Int): Matcher<View> = object : TypeSafeMatcher<View>() {

    private var actual: Int? = null

    override fun describeTo(description: Description) {
        description.appendText("RecyclerView with $expected items")
    }

    override fun describeMismatchSafely(item: View, mismatchDescription: Description) {
        mismatchDescription.appendText("had ${actual ?: "no adapter and so no"} items")
    }

    override fun matchesSafely(view: View): Boolean {
        val itemCount = (view as? RecyclerView)?.adapter?.itemCount ?: return false
        actual = itemCount
        return itemCount == expected
    }
}

/**
 * Matches a [RecyclerView] whose bound row at [position] matches [itemMatcher].
 *
 * Scoping to a row matters whenever the text under test repeats down the list — a bare
 * `withText` would match every row and fail as ambiguous.
 */
fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> =
    object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("has an item at position $position matching: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val holder = view.findViewHolderForAdapterPosition(position) ?: return false
            return itemMatcher.matches(holder.itemView)
        }
    }
