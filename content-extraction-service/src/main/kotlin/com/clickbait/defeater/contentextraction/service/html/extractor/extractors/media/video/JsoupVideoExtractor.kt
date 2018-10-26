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
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import mu.KLogging
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * A [Jsoup](https://jsoup.org/)-based media content [Extractor] implementation for extracting video media
 * content.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property naiveIFrameVideoExtractor a simple video extractor based on HTML iframe elements
 * @property brightCoveVideoExtractor video extractor specific to the
 * [BrightCove](https://www.brightcove.com/en/) platform
 * @property youTubeVideoExtractor video extractor specific to YouTube
 * @property cnetVideoExtractor video extractor specific to [Cnet](https://www.cnet.com/)
 * @property embedlyVideoExtractor video extractor specific to the
 * [Embedly](https://embed.ly/) platform
 * @property domainsToIgnore a list of domains this extractor should ignore and
 * therefore not extract
 */
@ExtractorBean(order = 4)
@Component
class JsoupVideoExtractor(
    private val naiveIFrameVideoExtractor: JsoupNaiveIFrameVideoExtractor,
    private val brightCoveVideoExtractor: JsoupBrightCoveVideoExtractor,
    private val youTubeVideoExtractor: JsoupYouTubeVideoExtractor,
    private val cnetVideoExtractor: JsoupCnetVideoExtractor,
    private val embedlyVideoExtractor: JsoupEmbedlyVideoExtractor
) : Extractor {

    private val domainsToIgnore = listOf("twitter.com/", "instagram.com/p/", "pinterest.com/")

    /**
     * Performs the extraction process on the given `source` and
     * (optionally) delegates to the next [Extractor] through
     * the given [ExtractorChain]. The result of this extractor
     * and of the chain are published through a single [Flux].
     *
     * @param source the source of a web page from which the
     * contents should be extracted
     * @param chain the chain to allow delegation to the next
     * [Extractor]
     * @return a Flux of [Content] extracted by this extractor
     * and optionally of other [Extractor]s in the chain (in
     * case of a delegation)
     */
    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        return Flux
            .concat(
                naiveIFrameVideoExtractor.extract(document),
                brightCoveVideoExtractor.extract(document),
                youTubeVideoExtractor.extract(document),
                cnetVideoExtractor.extract(document),
                embedlyVideoExtractor.extract(document)
            )
            .filter { doesNotContainIgnoredDomain(it as MediaContent) }
            .concatWith(chain.extract(source))
    }

    private fun doesNotContainIgnoredDomain(content: MediaContent): Boolean {
        for (domain in domainsToIgnore) {
            if (content.src.contains(domain)) {
                return false
            }
        }
        return true
    }

    companion object : KLogging()
}