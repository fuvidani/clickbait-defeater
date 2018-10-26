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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.social

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux

/**
 * Social media extractor specific to the [Instagram](https://www.instagram.com/) platform.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupInstagramSocialMediaContentExtractor {

    /**
     * Extracts multiple potential Instagram posts as social media
     * content from the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found social media [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("a[href*=instagram.com/p/]"))
            .map { SocialMediaContent(SocialMediaEmbeddingType.INSTAGRAM, getTrimmedInstagramUrl(it.attr("href"))) }
    }

    private fun getTrimmedInstagramUrl(source: String): String {
        val components = UriComponentsBuilder.fromUriString(source).build()
        return "https://www.instagram.com${components.path}"
    }
}