package com.sri.soundhar.newsapp_mvvm_architecture

import android.app.Application
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.ApplicationComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.component.DaggerApplicationComponent
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ApplicationModule


class NewsApplication : Application() {

    private lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
    }

    private fun injectDependencies() {
        applicationComponent = DaggerApplicationComponent
            .builder()
            .applicationModule(ApplicationModule(this))
            .build()
        applicationComponent.inject(this)
    }
}