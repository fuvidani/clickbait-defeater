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