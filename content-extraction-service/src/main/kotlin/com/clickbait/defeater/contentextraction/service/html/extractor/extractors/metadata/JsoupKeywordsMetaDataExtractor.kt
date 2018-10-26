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
import reactor.core.publisher.Flux

/**
 * Meta-data extractor specialized for extracting keywords from a Jsoup [Document].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JsoupKeywordsMetaDataExtractor {

    /**
     * Extracts keywords from the meta-data part of the provided
     * `document` and emits them in a [Flux].
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the extracted keywords
     */
    internal fun extract(document: Document): Flux<Content> {
        val keywords = document.getElementsByTag("meta")
            .filter {
                it.attr("name").isNotBlank() &&
                        it.attr("name").contains("keywords") &&
                        it.attr("content").isNotBlank()
            }.map { MetaDataContent(MetaDataType.KEYWORDS, it.attr("content")) }
            .firstOrNull()
        return if (keywords != null) {
            Flux.just(keywords)
        } else {
            Flux.empty()
        }
    }
}