package com.clickbait.defeater.contentextraction.web

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.service.ContentExtractionService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive, non-blocking REST controller for the ClickBait Content-Extraction-Service.
 * An extensive REST documentation in HTML format can be found in the resources.
 *
 * @property contentExtractionService an implementation of the [ContentExtractionService] interface
 * supporting all its operations
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/content")
class ContentExtractionController(private val contentExtractionService: ContentExtractionService) {

    /**
     * Retrieves the relevant contents for the given `url` as a continuous stream of
     * [Content] objects. This endpoint is intended only for use-cases, where the
     * non-blocking streaming of extracted contents (with back-pressure) is desired.
     * For a composite result, the [extractRelevantContent] endpoint should be preferred.
     *
     * @param url the absolute, decoded URL of the web page/article for which the contents
     * should be extracted
     * @param title optional title of the web page, in case the client already possesses
     * this information
     * @return a stream of [Content] implementations emitted by a Flux
     */
    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_STREAM_JSON_VALUE])
    fun extractRelevantContentAsStream(
        @RequestParam url: String,
        @RequestParam(defaultValue = "") title: String
    ): Flux<Content> {
        return contentExtractionService
            .extractContent(WebPage(url, title))
            .flatMapMany { Flux.fromIterable(it.contents) }
    }

    /**
     * Retrieves the relevant contents for the given `url` and returns a composite
     * result in a [ContentWrapper] object.
     *
     * @param url the absolute, decoded URL of the web page/article for which the contents
     * should be extracted
     * @param title optional title of the web page, in case the client already possesses
     * this information
     * @return a Mono emitting the [ContentWrapper] containing the extracted contents
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun extractRelevantContent(
        @RequestParam url: String,
        @RequestParam(defaultValue = "") title: String
    ): Mono<ContentWrapper> {
        return contentExtractionService.extractContent(WebPage(url, title))
    }
}