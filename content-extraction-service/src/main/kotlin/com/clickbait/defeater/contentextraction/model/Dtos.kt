package com.clickbait.defeater.contentextraction.model

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
data class PostInstance(
    val id: String,
    val language: String = "unknown",
    val postText: List<String>,
    val postTimestamp: String = "",
    val postMedia: List<String> = emptyList(),
    val targetTitle: String = "",
    val targetDescription: String = "",
    val targetKeywords: String = "",
    val targetParagraphs: List<String> = emptyList()
)

data class WebPage(
    val url: String,
    val title: String = ""
)

data class WebPageSource(
    val url: String,
    val title: String,
    val html: String = ""
)

data class Contents(val contents: List<Content>)