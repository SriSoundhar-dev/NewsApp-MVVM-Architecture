package com.sri.soundhar.newsapp_mvvm_architecture.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [Dispatchers.Main] for a [TestDispatcher] so anything launched in a `viewModelScope`
 * runs on the test's virtual clock instead of the Android main looper, which does not exist
 * in a JVM unit test.
 *
 * Defaults to [StandardTestDispatcher], so coroutines queue up rather than running eagerly —
 * that lets a test assert the initial state before calling `advanceUntilIdle()`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
