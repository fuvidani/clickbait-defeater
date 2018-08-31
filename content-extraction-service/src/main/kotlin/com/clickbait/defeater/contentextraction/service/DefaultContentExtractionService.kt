package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.persistence.ContentDataStore
import com.clickbait.defeater.contentextraction.service.handler.ContentExtractionHandler
import org.springframework.stereotype.Component
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
@Component
class DefaultContentExtractionService(
    private val handler: ContentExtractionHandler,
    private val store: ContentDataStore
) : ContentExtractionService {

    override fun extractContent(webPage: WebPage): Mono<ContentWrapper> {
        return store
            .findById(webPage.url)
            .switchIfEmpty(
                Mono.defer {
                    handler.extract(webPage)
                        .flatMap { store.save(it) }
                }
            )
    }
}