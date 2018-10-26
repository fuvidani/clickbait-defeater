package com.clickbait.defeater.contentextraction.service.html.extractor

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import reactor.core.publisher.Flux

/**
 * Contract to allow an [Extractor] to delegate to the next one
 * in the chain. This contract/structure follows the pattern
 * described in [org.springframework.web.server.WebFilterChain].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ExtractorChain {

    /**
     * Delegate to the next [Extractor] in the chain.
     *
     * @param source the web page source each [Extractor]
     * should process
     * @return a Flux of [Content] published by the chain;
     * the end of the chain emits a complete signal thus
     * ensuring a finite stream
     */
    fun extract(source: WebPageSource): Flux<Content>
}