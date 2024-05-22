package com.sri.soundhar.newsapp_mvvm_architecture.di.component

import com.sri.soundhar.newsapp_mvvm_architecture.di.ActivityScope
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ActivityModule
import com.sri.soundhar.newsapp_mvvm_architecture.di.module.ApplicationModule
import com.sri.soundhar.newsapp_mvvm_architecture.ui.topheadline.TopHeadlineActivity
import dagger.Component
@ActivityScope
@Component(dependencies = [ApplicationComponent::class], modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(activity: TopHeadlineActivity)

}