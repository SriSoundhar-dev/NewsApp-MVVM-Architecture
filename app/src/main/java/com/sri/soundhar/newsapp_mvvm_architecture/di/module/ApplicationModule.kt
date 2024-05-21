package com.sri.soundhar.newsapp_mvvm_architecture.di.module

import android.content.Context
import com.sri.soundhar.newsapp_mvvm_architecture.NewsApplication
import com.sri.soundhar.newsapp_mvvm_architecture.di.ApplicationContext
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val application:NewsApplication) {

    @ApplicationContext
    @Provides
    fun provideContext():Context{
        return application
    }
}