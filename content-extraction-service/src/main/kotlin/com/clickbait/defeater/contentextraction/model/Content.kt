package com.clickbait.defeater.contentextraction.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
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
        JsonSubTypes.Type(value = MetaDataContent::class, name = "META_DATA")]
)
interface Content {

    val contentType: ContentType
}

enum class ContentType {
    TEXT,
    MEDIA,
    SOCIAL_MEDIA,
    META_DATA
}