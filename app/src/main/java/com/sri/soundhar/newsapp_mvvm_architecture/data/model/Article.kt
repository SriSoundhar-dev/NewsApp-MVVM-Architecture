package com.sri.soundhar.newsapp_mvvm_architecture.data.model

import com.google.gson.annotations.SerializedName

/**
 * Note that [description], [imageUrl] and [source] are nullable: newsapi.org genuinely
 * returns `null` for them on some articles.
 *
 * Keeping every field defaulted also matters for a subtler reason — Kotlin only synthesises
 * a no-arg constructor when all parameters have defaults, and without one Gson allocates the
 * instance through Unsafe and skips the constructor, so none of the defaults below would
 * ever run.
 */
data class Article(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("url")
    val url: String = "",
    @SerializedName("urlToImage")
    val imageUrl: String? = null,
    @SerializedName("source")
    val source: Source? = null,
)
