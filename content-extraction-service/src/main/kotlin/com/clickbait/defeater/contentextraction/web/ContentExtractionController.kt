package com.clickbait.defeater.contentextraction.web

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.PostInstance
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.service.ContentExtractionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    @PostMapping("/extract")
    fun extractRelevantContent(@RequestBody webPage: WebPage): Flux<Content> {
        return contentExtractionService.extractContent(webPage)
    }

    @PostMapping("/completePost")
    fun getCompletePostInstance(@RequestBody instance: PostInstance): Mono<PostInstance> {
        return contentExtractionService.getCompletePostInstanceOf(instance)
    }
}