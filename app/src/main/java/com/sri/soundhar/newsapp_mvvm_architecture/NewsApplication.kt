package com.sri.soundhar.newsapp_mvvm_architecture

import android.app.Application
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.ApplicationComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.DaggerApplicationComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ApplicationModule


open class NewsApplication : Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
    }

    /**
     * Builds the graph the app runs on. Instrumentation tests override this to swap in a
     * component pointed at a local MockWebServer instead of newsapi.org.
     */
    protected open fun buildApplicationComponent(): ApplicationComponent =
        DaggerApplicationComponent
            .builder()
            .applicationModule(ApplicationModule(this))
            .build()

    private fun injectDependencies() {
        applicationComponent = buildApplicationComponent()
        applicationComponent.inject(this)
    }
}