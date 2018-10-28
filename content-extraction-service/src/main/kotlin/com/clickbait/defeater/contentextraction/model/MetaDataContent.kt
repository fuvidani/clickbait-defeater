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
 * A meta-data [Content] with a `data` attribute containing
 * the actual data.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property type the type of meta-data
 * @property data the data itself; can be word, sentence,
 * list of words or even an URL
 */
data class MetaDataContent(
    val type: MetaDataType,
    val data: String
) : Content {
    override val contentType = ContentType.META_DATA
}

/**
 * Enumeration of the meta-data types.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
enum class MetaDataType {
    TITLE,
    DESCRIPTION,
    KEYWORDS,
    LANGUAGE,
    IMAGE,
    VIDEO,
    TIMESTAMP
}