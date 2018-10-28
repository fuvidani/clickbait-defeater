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

package com.clickbait.defeater.contentextraction.service.handler

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.SocialMediaContent
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.HtmlProvider
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Implementation of the [ContentExtractionHandler] interface.
 * For an extraction, first the web page's HTML source is obtained
 * via an [HtmlProvider] instance. This information is then used to
 * invoke an [ExtractorChain] which performs the extraction steps
 * for getting the extracted contents.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property htmlProvider a reactive [HtmlProvider] implementation
 * capable of retrieving the source code of a particular web page
 * @property extractorChain a chain of extractors doing the extraction
 */
@Component
class DefaultContentExtractionHandler(
    private val htmlProvider: HtmlProvider,
    private val extractorChain: ExtractorChain
) : ContentExtractionHandler {

    /**
     * Processes the input `webPage`, performs content extraction
     * and returns the result in a [ContentWrapper] object emitted
     * by a [Mono].
     *
     * @param webPage a valid [WebPage] object as input, describing
     * the web page of which the contents should be extracted
     * @return a Mono emitting the extracted contents
     */
    override fun extract(webPage: WebPage): Mono<ContentWrapper> {
        return htmlProvider
            .get(webPage)
            .zipWhen { extractContents(it).collectList() }
            .map {
                val webPageSource = it.t1
                val contents = it.t2
                ContentWrapper(webPageSource.redirectUrl, webPageSource.sourceUrl, contents)
            }
    }

    /**
     * Invokes the `extractorChain` and subsequently applies a
     * [Flux.distinct] operator to filter our duplicate contents.
     * This can happen for example if a YouTube video gets extracted
     * by more than one extractors.
     */
    private fun extractContents(source: WebPageSource): Flux<Content> {
        return extractorChain
            .extract(source)
            .distinct {
                when (it) {
                    is MediaContent -> it.src
                    is SocialMediaContent -> it.src
                    else -> it.hashCode().toString()
                }
            }
    }
}