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

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * A wrapper object encapsulating the redirect- and source-URL of a
 * certain web page (they aren't necessarily different) along with
 * a list of [Content] objects that this particular web page contains.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property redirectUrl the originally obtained URL which may be a
 * statically or dynamically redirected one. In this case the `redirectUrl`
 * differs from the `sourceUrl`.
 * @property sourceUrl the actual URL of the web page, i.e. it is not a
 * redirect one.
 * @property contents list of [Content] objects describing the web page's
 * content
 */
@Document(collection = "contents")
data class ContentWrapper(
    @Id
    val redirectUrl: String,
    val sourceUrl: String,
    val contents: List<Content>
)