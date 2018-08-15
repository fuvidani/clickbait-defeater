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
data class SocialMediaContent(
    val type: SocialMediaEmbeddingType,
    val src: String
) : Content {
    override val contentType = ContentType.SOCIAL_MEDIA
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