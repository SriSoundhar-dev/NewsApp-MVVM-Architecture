package com.sri.soundhar.newsapp_mvvm_architecture

import com.sri.soundhar.newsapp_mvvm_architecture.di.DaggerTestApplicationComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.TestApplicationModule
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.ApplicationComponent

/**
 * Installed by [NewsTestRunner] in place of [NewsApplication], so the whole app runs against
 * a local MockWebServer instead of newsapi.org.
 */
class TestNewsApplication : NewsApplication() {

    override fun buildApplicationComponent(): ApplicationComponent =
        DaggerTestApplicationComponent.builder()
            .testApplicationModule(TestApplicationModule(this))
            .build()
}
