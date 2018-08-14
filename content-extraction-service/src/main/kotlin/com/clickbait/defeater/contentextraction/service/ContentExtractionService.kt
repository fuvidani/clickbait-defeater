package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.PostInstance
import com.clickbait.defeater.contentextraction.model.WebPage
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
interface ContentExtractionService {

    fun extractContent(webPage: WebPage): Flux<Content>

    fun getCompletePostInstanceOf(instance: PostInstance): Mono<PostInstance>
}