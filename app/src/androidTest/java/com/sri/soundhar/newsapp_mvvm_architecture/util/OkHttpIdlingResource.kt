package com.sri.soundhar.newsapp_mvvm_architecture.util

import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient

/**
 * Makes Espresso wait for in-flight OkHttp calls.
 *
 * Without it, an assertion can run before MockWebServer's reply has been parsed and pushed
 * into the UI — passing or failing depending on timing.
 */
class OkHttpIdlingResource(
    private val client: OkHttpClient,
    private val resourceName: String = "OkHttp",
) : IdlingResource {

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    init {
        client.dispatcher.idleCallback = Runnable { callback?.onTransitionToIdle() }
    }

    override fun getName(): String = resourceName

    override fun isIdleNow(): Boolean = client.dispatcher.runningCallsCount() == 0

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }
}
