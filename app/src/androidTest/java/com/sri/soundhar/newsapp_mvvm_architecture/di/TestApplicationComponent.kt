package com.sri.soundhar.newsapp_mvvm_architecture.di

import com.sri.soundhar.newsapp_mvvm_architecture.di.component.ApplicationComponent
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Stands in for the real `ApplicationComponent`. It extends it so `TopHeadlineActivity`,
 * which reads `NewsApplication.applicationComponent`, is none the wiser — and exposes the
 * [OkHttpClient] so tests can register an IdlingResource against it.
 */
@Singleton
@Component(modules = [TestApplicationModule::class])
interface TestApplicationComponent : ApplicationComponent {

    fun getOkHttpClient(): OkHttpClient
}
