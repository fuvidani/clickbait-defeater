package com.clickbait.defeater.contentextraction.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Base interface for all contents that can be extracted by content-extraction
 * procedures. Every implementation shares an attribute `contentType` which
 * differentiates the major content types as defined in the [ContentType]
 * enumeration.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property contentType the top-level type of the content
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "contentType",
    visible = true
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = TextContent::class, name = "TEXT"),
        JsonSubTypes.Type(value = MediaContent::class, name = "MEDIA"),
        JsonSubTypes.Type(value = SocialMediaContent::class, name = "SOCIAL_MEDIA"),
        JsonSubTypes.Type(value = MetaDataContent::class, name = "META_DATA"),
        JsonSubTypes.Type(value = HtmlContent::class, name = "HTML")]
)
interface Content {

    val contentType: ContentType
}

/**
 * Enumeration of the top-level content types.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
enum class ContentType {
    /**
     * A textual content like a word, sentence or paragraph.
     */
    TEXT,
    /**
     * Media content like an image, audio or video.
     * @see MediaType
     */
    MEDIA,
    /**
     * Social media content such as Twitter or Facebook.
     * @see SocialMediaEmbeddingType
     */
    SOCIAL_MEDIA,
    /**
     * A meta-data content like a description or a title.
     * @see MetaDataType
     */
    META_DATA,
    /**
     * This content type implies raw HTML code as content, ready
     * to be embedded
     */
    HTML
}