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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DefaultContentExtractionHandler(
    private val htmlProvider: HtmlProvider,
    private val extractorChain: ExtractorChain
) : ContentExtractionHandler {

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