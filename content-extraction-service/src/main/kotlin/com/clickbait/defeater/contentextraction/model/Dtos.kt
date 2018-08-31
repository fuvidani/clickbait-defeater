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
    val redirectUrl: String,
    val sourceUrl: String,
    val title: String,
    val html: String = ""
)

data class MercuryApiResponse(
    val title: String?,
    val content: String?,
    val date_published: String?,
    val lead_image_url: String?,
    val dek: String?,
    val url: String?,
    val domain: String?,
    val excerpt: String?,
    val word_count: Int?,
    val direction: String?,
    val total_pages: Int?,
    val rendered_pages: Int?,
    val next_page_url: String?
)
