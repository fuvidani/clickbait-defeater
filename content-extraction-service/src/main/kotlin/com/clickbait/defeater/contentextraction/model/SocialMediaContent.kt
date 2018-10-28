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

package com.clickbait.defeater.contentextraction.model

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