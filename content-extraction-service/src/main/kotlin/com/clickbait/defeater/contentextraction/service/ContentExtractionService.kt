package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.WebPage
import reactor.core.publisher.Mono

/**
 * Top-level interface for describing operations a [ContentExtractionService]
 * must support.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ContentExtractionService {

    /**
     * Extracts as much structured content as possible from the
     * provided `webPage` and returns it as a [ContentWrapper]
     * published by a [Mono].
     *
     * @param webPage a valid [WebPage] object as input, describing
     * the web page of which the contents should be extracted
     * @return a Mono emitting the extracted contents
     */
    fun extractContent(webPage: WebPage): Mono<ContentWrapper>
}