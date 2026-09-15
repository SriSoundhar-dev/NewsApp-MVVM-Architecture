package com.sri.soundhar.newsapp_mvvm_architecture.uitils

import com.sri.soundhar.newsapp_mvvm_architecture.BuildConfig

object AppConstant {

    /**
     * Supplied at build time from `local.properties` (or a `NEWS_API_KEY` environment
     * variable in CI) rather than checked in — see the README. Empty when unset, which
     * builds fine and fails at runtime with a 401.
     */
    const val API_KEY = BuildConfig.NEWS_API_KEY
    const val COUNTRY = "us"
}
