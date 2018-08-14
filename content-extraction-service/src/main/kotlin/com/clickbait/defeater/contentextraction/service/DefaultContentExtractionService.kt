package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.PostInstance
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.persistence.ContentDataStore
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
import com.clickbait.defeater.contentextraction.service.mapper.ContentMapper
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
    private val chain: ExtractorChain,
    private val store: ContentDataStore
) : ContentExtractionService {

    override fun extractContent(webPage: WebPage): Mono<ContentWrapper> {
        return store.findById(webPage.url)
            .switchIfEmpty(
                Mono.defer {
                    chain.extract(WebPageSource(webPage.url, webPage.title))
                        .collectList()
                        .flatMap { store.save(ContentWrapper(webPage.url, it)) }
                }
            )
    }

    override fun getCompletePostInstanceOf(instance: PostInstance): Mono<PostInstance> {
        return extractContent(WebPage(instance.id, instance.targetTitle))
            .map { ContentMapper.toCompletePostInstance(instance, it) }
    }
}