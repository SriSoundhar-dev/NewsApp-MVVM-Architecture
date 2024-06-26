package com.sri.soundhar.newsapp_mvvm_architecture.di.module

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.sri.soundhar.newsapp_mvvm_architecture.data.repository.TopHeadlineRepository
import com.sri.soundhar.newsapp_mvvm_architecture.di.ActivityContext
import com.sri.soundhar.newsapp_mvvm_architecture.ui.base.ViewModelProviderFactory
import com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline.TopHeadlineAdapter
import com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline.TopHeadlineViewModel
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: ComponentActivity) {

    @ActivityContext
    @Provides
    fun provideContext(): Context {
        return activity
    }

    @Provides
    fun provideNewsListViewModel(topHeadlineRepository: TopHeadlineRepository): TopHeadlineViewModel {
        return ViewModelProvider(activity,
            ViewModelProviderFactory(TopHeadlineViewModel::class) {
                TopHeadlineViewModel(topHeadlineRepository)
            })[TopHeadlineViewModel::class.java]
    }

    @Provides
    fun provideTopHeadlineAdapter() = TopHeadlineAdapter(ArrayList())
}