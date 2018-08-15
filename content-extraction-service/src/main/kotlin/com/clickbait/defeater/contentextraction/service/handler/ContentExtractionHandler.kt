package com.clickbait.defeater.contentextraction.service.handler

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPage
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
interface ContentExtractionHandler {

    fun extract(webPage: WebPage): Flux<Content>
}