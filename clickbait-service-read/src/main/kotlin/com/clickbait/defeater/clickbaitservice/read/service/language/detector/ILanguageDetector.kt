package com.clickbait.defeater.clickbaitservice.read.service.language.detector

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
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
interface ILanguageDetector {

    fun detect(instance: PostInstance): Mono<PostInstance>
}