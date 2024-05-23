package com.sri.soundhar.newsapp_mvvm_architecture.di.component

import android.content.Context
import com.sri.soundhar.newsapp_mvvm_architecture.NewsApplication
import com.sri.soundhar.newsapp_mvvm_architecture.data.api.NetworkService
import com.sri.soundhar.newsapp_mvvm_architecture.data.repository.TopHeadlineRepository
import com.sri.soundhar.newsapp_mvvm_architecture.di.ApplicationContext
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ApplicationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    fun inject(application: NewsApplication)

    @ApplicationContext
    fun getContext(): Context

    fun getNetworkService(): NetworkService

    fun getTopHeadlineRepository(): TopHeadlineRepository
}