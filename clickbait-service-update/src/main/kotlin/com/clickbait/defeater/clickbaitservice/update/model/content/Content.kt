/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.clickbaitservice.update.model.content

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