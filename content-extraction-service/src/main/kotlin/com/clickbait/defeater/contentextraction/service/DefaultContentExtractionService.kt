package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
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
class DefaultContentExtractionService(private val chain: ExtractorChain) : ContentExtractionService {

    override fun extractContent(webPage: WebPage): Flux<Content> {
        return chain.extract(WebPageSource(webPage.url, webPage.title, ""))
    }
}