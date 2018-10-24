package com.clickbait.defeater.clickbaitservice.update.model.content

/**
 * A social media [Content] with a `src` attribute.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property type the type of the social media content
 * @property src the source of the social media embedding,
 * usually an absolute URL which can be independently opened
 */
data class SocialMediaContent(
    val type: SocialMediaEmbeddingType,
    val src: String
) : Content {
    override val contentType = ContentType.SOCIAL_MEDIA
}

/**
 * Enumeration of the supported social media embedding types.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
enum class SocialMediaEmbeddingType {
    /**
     * Content/Embedding from Twitter.
     */
    TWITTER,
    /**
     * Content/Embedding from Instagram.
     */
    INSTAGRAM,
    /**
     * Content/Embedding from Pinterest.
     */
    PINTEREST
    /**
     * Other possible types:
     * - TUMBLR
     * - FLICKR
     * ....
     */
}