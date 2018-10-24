package com.clickbait.defeater.clickbaitservice.update.model.content

/**
 * A media [Content] with a `src` attribute.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property type the type of media
 * @property src the source of the media content,
 * usually an absolute URL
 */
data class MediaContent(
    val type: MediaType,
    val src: String
) : Content {
    override val contentType = ContentType.MEDIA
}

/**
 * Enumeration of all media types.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
enum class MediaType {
    /**
     * Image media type
     */
    IMAGE,
    /**
     * Audio media type
     */
    AUDIO,
    /**
     * Video media type
     */
    VIDEO
}