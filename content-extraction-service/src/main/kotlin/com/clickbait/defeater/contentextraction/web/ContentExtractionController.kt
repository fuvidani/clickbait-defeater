package com.clickbait.defeater.contentextraction.web

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.PostInstance
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.service.ContentExtractionService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @PostMapping("/extract",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_STREAM_JSON_VALUE])
    fun extractRelevantContentAsStream(@RequestBody webPage: WebPage): Flux<Content> {
        return contentExtractionService.extractContent(webPage)
            .flatMapMany { Flux.fromIterable(it.contents) }
    }

    @PostMapping("/extract", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun extractRelevantContent(@RequestBody webPage: WebPage): Mono<ContentWrapper> {
        return contentExtractionService.extractContent(webPage)
    }

    @PostMapping("/completePost")
    fun getCompletePostInstance(@RequestBody instance: PostInstance): Mono<PostInstance> {
        return contentExtractionService.getCompletePostInstanceOf(instance)
    }
}