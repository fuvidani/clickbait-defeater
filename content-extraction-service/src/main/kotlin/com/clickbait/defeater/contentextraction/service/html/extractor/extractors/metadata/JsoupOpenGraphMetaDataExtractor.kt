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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MetaDataContent
import com.clickbait.defeater.contentextraction.model.MetaDataType
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

/**
 * Meta-data extractor specialized for extracting the Facebook [Open Graph](http://ogp.me/)
 * attributes from a Jsoup [Document].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JsoupOpenGraphMetaDataExtractor {

    /**
     * Extracts the supported Open-Graph meta-data values
     * from the given `document` and emits them in a [Flux]
     * as [Content]s.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found contents
     */
    internal fun extract(document: Document): Flux<Content> {
        return document
            .getElementsByTag("meta")
            .filter { isOgElement(it) }
            .mapNotNull { ogElementToContent(it) }
            .toFlux()
    }

    private fun isOgElement(element: Element): Boolean {
        val property = element.attr("property")
        val content = element.attr("content")
        return property.isNotBlank() &&
                content.isNotBlank() &&
                property.startsWith("og:")
    }

    private fun ogElementToContent(element: Element): Content? {
        val ogContent = element.attr("content")
        val ogProperty = element.attr("property")
        return when (ogProperty) {
            "og:title" -> MetaDataContent(MetaDataType.TITLE, ogContent)
            "og:description" -> MetaDataContent(MetaDataType.DESCRIPTION, ogContent)
            "og:image" -> MetaDataContent(MetaDataType.IMAGE, ogContent)
            "og:video" -> MetaDataContent(MetaDataType.VIDEO, ogContent)
            "og:updated_time" -> MetaDataContent(MetaDataType.TIMESTAMP, ogContent)
            // add newly supported og tag<->content mapping here
            else -> null
        }
    }
}