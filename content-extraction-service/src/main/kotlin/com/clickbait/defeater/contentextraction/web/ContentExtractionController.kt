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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/content")
class ContentExtractionController(private val contentExtractionService: ContentExtractionService) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_STREAM_JSON_VALUE])
    fun extractRelevantContentAsStream(
        @RequestParam url: String,
        @RequestParam(defaultValue = "") title: String
    ): Flux<Content> {
        return contentExtractionService
            .extractContent(WebPage(url, title))
            .flatMapMany { Flux.fromIterable(it.contents) }
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun extractRelevantContent(
        @RequestParam url: String,
        @RequestParam(defaultValue = "") title: String
    ): Mono<ContentWrapper> {
        return contentExtractionService.extractContent(WebPage(url, title))
    }
}