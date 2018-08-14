package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.PostInstance
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.persistence.cache.ContentCache
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
import com.clickbait.defeater.contentextraction.service.mapper.ContentMapper
import org.springframework.stereotype.Component
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
@Component
class DefaultContentExtractionService(
    private val chain: ExtractorChain,
    private val cache: ContentCache
) : ContentExtractionService {

    override fun extractContent(webPage: WebPage): Flux<Content> {
        return cache.tryAndGet(webPage)
            .switchIfEmpty(
                Flux.defer {
                    chain.extract(WebPageSource(webPage.url, webPage.title))
                        .collectList()
                        .flatMapMany { cache.put(webPage, it) }
                }
            )
    }

    override fun getCompletePostInstanceOf(instance: PostInstance): Mono<PostInstance> {
        return extractContent(WebPage(instance.id, instance.targetTitle))
            .collectList()
            .map { ContentMapper.toCompletePostInstance(instance, it) }
    }
}