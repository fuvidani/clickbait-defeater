package com.clickbait.defeater.clickbaitservice.update.model.content

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
data class MediaContent(
    val type: MediaType,
    val src: String
) : Content {
    override val contentType = ContentType.MEDIA
}

enum class MediaType {
    IMAGE,
    AUDIO,
    VIDEO
}