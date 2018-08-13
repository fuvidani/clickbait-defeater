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