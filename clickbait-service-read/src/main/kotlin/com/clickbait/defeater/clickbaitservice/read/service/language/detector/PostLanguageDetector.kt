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
class PostLanguageDetector(private val languageDetector: LanguageDetector) : ILanguageDetector {

    override fun detect(instance: PostInstance): Mono<PostInstance> {
        return Mono.create {
            val lang = languageDetector.detect(instance.postText.joinToString(separator = ". "))
            val locale = lang.get()
            it.success(instance.withLanguage(locale.language))
        }
    }
}