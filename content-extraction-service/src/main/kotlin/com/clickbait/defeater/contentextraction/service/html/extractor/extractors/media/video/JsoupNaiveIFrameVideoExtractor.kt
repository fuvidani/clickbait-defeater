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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import mu.KLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Video media extractor specialized for HTML iframe elements.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupNaiveIFrameVideoExtractor {

    // matches 10, 100, 100px, 100%, ...
    // doesn't match 0, 1, 2
    private val regex = "\\d{2,}(px|%)*"
    private val blackListSourceChunks = listOf("youtube.com/", "brightcove", "plugins/like.php?", "/recaptcha", ".php")

    /**
     * Extracts multiple potential iframe-based video content from
     * the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found video [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("iframe[width~=$regex],  iframe[height~=$regex]"))
            .filter { shouldKeep(it) }
            .map { MediaContent(MediaType.VIDEO, it.attr("src")) }
            .doOnNext { logger.info("IFrame video found (${it.src})") }
            .map { it as Content }
    }

    private fun shouldKeep(element: Element): Boolean {
        val src = element.attr("src")
        return src.isNotEmpty() && isNotComposedOfChunks(src)
    }

    private fun isNotComposedOfChunks(source: String): Boolean {
        for (chunk in blackListSourceChunks) {
            if (source.contains(chunk)) {
                return false
            }
        }
        return true
    }

    companion object : KLogging()
}