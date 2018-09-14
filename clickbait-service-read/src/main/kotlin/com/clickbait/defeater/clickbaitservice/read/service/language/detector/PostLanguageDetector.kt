package com.clickbait.defeater.clickbaitservice.read.service.language.detector

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.optimaize.langdetect.LanguageDetector
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
class PostLanguageDetector(private val languageDetector: LanguageDetector) :
    com.clickbait.defeater.clickbaitservice.read.service.language.detector.LanguageDetector {

    override fun detect(instance: PostInstance): Mono<PostInstance> {
        return Mono.create {
            val probabilities = languageDetector.getProbabilities(instance.postText.joinToString(separator = ". "))
            if (probabilities.isEmpty()) {
                it.success(instance.withLanguage("unknown"))
            } else {
                it.success(instance.withLanguage(probabilities[0].locale.language))
            }
        }
    }
}