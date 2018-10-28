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
 * Class representing a simple web page with its
 * URL and optional title.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property url absolute URL of the web page, which may
 * also be a statically (or dynamically) redirect one
 * @property title optional title of the web page
 */
data class WebPage(
    val url: String,
    val title: String = ""
)

/**
 * Class extending the simple representation of
 * a web page. It also encapsulates the HTML
 * source of it and differentiates between the
 * actual URL and the redirect URL which this
 * web page may have been originally accessed with.
 *
 * @property redirectUrl absolute URL which is generally
 * considered to be a redirect one. May be equal to `sourceUrl`.
 * @property sourceUrl actual absolute URL of the target web
 * page. The `redirectUrl` and `sourceUrl` attributes only differ,
 * if `redirectUrl` was an actual redirect one.
 * @property title of the web page
 * @property html the HTML source code of the web page in a
 * String representation. Common HTML parsers should be able to
 * parse this into a valid HTML document.
 */
data class WebPageSource(
    val redirectUrl: String,
    val sourceUrl: String,
    val title: String,
    val html: String = ""
)

/**
 * Data class for representing the API response of the
 * Mercury Web Parser as described [here](https://mercury.postlight.com/web-parser/).
 *
 * @property title title of the processed web page
 * @property content relevant content as HTML code
 * @property date_published timestamp when the web page has been published
 * @property lead_image_url absolute URL of the web page's lead image
 * @property dek there is no clear description what this attribute
 * represents
 * @property url absolute URL of the web page
 * @property domain domain of the web page
 * @property excerpt a small textual excerpt of the page, usually
 * found in the meta-data
 * @property word_count word count of the content
 * @property direction how the web page's content should be read.
 * Might be "ltr"(left-to-right) or "rtl"(right-to-left)
 * @property total_pages the number of pages if the web page consists
 * of multiple ones
 * @property rendered_pages the number of rendered pages
 * @property next_page_url the URL of the next page if this web page
 * consists of multiple ones and there is a next one
 */
data class MercuryApiResponse(
    val title: String?,
    val content: String?,
    val date_published: String?,
    val lead_image_url: String?,
    val dek: String?,
    val url: String?,
    val domain: String?,
    val excerpt: String?,
    val word_count: Int?,
    val direction: String?,
    val total_pages: Int?,
    val rendered_pages: Int?,
    val next_page_url: String?
)
