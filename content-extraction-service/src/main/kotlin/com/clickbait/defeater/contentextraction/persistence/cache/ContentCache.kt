package com.clickbait.defeater.contentextraction.persistence.cache

import com.clickbait.defeater.contentextraction.model.ContentWrapper
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
interface ContentCache {

    fun tryAndGet(url: String): Mono<ContentWrapper>

    fun put(contentWrapper: ContentWrapper): Mono<Boolean>
}