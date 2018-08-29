package com.clickbait.defeater.contentextraction.service.handler

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.HtmlProvider
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

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

    override fun extract(webPage: WebPage): Flux<Content> {
        return htmlProvider
            .get(webPage)
            .map { WebPageSource(webPage.url, webPage.title, it) }
            .flatMapMany { extractorChain.extract(it) }
    }
}