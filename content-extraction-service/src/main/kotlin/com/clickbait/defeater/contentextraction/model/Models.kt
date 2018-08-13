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

interface Content

data class TextContent(val text: String) : Content

data class MediaContent(
    val type: MediaType,
    val src: String
) : Content

data class SocialMediaContent(
    val type: SocialMediaEmbeddingType,
    val src: String
) : Content

enum class MediaType {
    IMAGE,
    AUDIO,
    VIDEO
}

enum class SocialMediaEmbeddingType {
    TWITTER,
    INSTAGRAM
    /**
     * Other possible types:
     * - PINTEREST
     * - TUMBLR
     * - FLICKR
     * ....
     */
}

data class WebPageSource(
    val url: String,
    val title: String,
    val html: String
)

/*
data class OldContent(val url: String,
                      val textContents: List<TextContent> = emptyList(),
                      val mediaContents: List<MediaContent> = emptyList(),
                      val socialMediaContents: List<SocialMediaContent> = emptyList())
 */