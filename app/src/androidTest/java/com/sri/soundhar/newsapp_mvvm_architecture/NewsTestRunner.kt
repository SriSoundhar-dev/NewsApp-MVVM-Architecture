package com.sri.soundhar.newsapp_mvvm_architecture

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Swaps [NewsApplication] for [TestNewsApplication] before the app process starts.
 * Registered as the module's `testInstrumentationRunner`.
 */
class NewsTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, TestNewsApplication::class.java.name, context)
}
