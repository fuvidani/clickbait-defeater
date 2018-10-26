package com.clickbait.defeater.contentextraction.service.handler

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.WebPage
import reactor.core.publisher.Mono

/**
 * Interface for abstracting away the actual content extraction procedure.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ContentExtractionHandler {

    /**
     * Processes the input `webPage`, performs content extraction
     * and returns the result in a [ContentWrapper] object emitted
     * by a [Mono].
     *
     * @param webPage a valid [WebPage] object as input, describing
     * the web page of which the contents should be extracted
     * @return a Mono emitting the extracted contents
     */
    fun extract(webPage: WebPage): Mono<ContentWrapper>
}