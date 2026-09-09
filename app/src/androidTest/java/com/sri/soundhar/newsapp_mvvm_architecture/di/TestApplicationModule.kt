package com.sri.soundhar.newsapp_mvvm_architecture.di

import android.content.Context
import com.sri.soundhar.newsapp_mvvm_architecture.NewsApplication
import com.sri.soundhar.newsapp_mvvm_architecture.data.api.NetworkService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Mirrors `ApplicationModule`, with two deliberate differences:
 *
 *  - the base URL points at the MockWebServer the tests run on [MOCK_SERVER_PORT], and
 *  - Retrofit is given an explicit [OkHttpClient] so the tests can hang an Espresso
 *    IdlingResource off its dispatcher and actually wait for responses.
 */
@Module
class TestApplicationModule(private val application: NewsApplication) {

    @ApplicationContext
    @Provides
    fun provideContext(): Context = application

    @BaseUrl
    @Provides
    fun provideBaseUrl(): String = "http://127.0.0.1:$MOCK_SERVER_PORT/"

    @Provides
    @Singleton
    fun provideGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideNetworkService(
        @BaseUrl baseUrl: String,
        gsonConverterFactory: GsonConverterFactory,
        okHttpClient: OkHttpClient,
    ): NetworkService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(NetworkService::class.java)
}

/**
 * Fixed because the graph is built when the app process starts, before any test has run and
 * so before MockWebServer could report a randomly allocated port.
 */
const val MOCK_SERVER_PORT = 8080
