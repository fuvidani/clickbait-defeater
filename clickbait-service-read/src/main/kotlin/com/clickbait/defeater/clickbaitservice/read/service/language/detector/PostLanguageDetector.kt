package com.clickbait.defeater.clickbaitservice.read.service.language.detector

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.optimaize.langdetect.LanguageDetector
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [com.clickbait.defeater.clickbaitservice.read.service.language.detector.LanguageDetector]
 * interface. This implementation uses the an external library [com.optimaize.langdetect.LanguageDetector] in
 * order to efficiently determine the most probable language of a string.
 *
 * @property languageDetector an implementation of the external library's [com.optimaize.langdetect.LanguageDetector]
 * interface
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class PostLanguageDetector(private val languageDetector: LanguageDetector) :
    com.clickbait.defeater.clickbaitservice.read.service.language.detector.LanguageDetector {

    /**
     * Analyzes the provided `instance`, tries to determine the language
     * of its contents and returns it extended by the correct language
     * attribute.
     *
     * @param instance a valid social media post instance s.t. its language
     * is questionable (either set or unknown)
     * @return a Mono emitting the same [PostInstance] object with the only
     * difference being in the `language` attribute
     */
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