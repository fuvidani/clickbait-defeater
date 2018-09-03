package com.clickbait.defeater.clickbaitservice.read.service.language.checker

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import com.clickbait.defeater.clickbaitservice.read.service.language.detector.ILanguageDetector
import org.springframework.http.HttpStatus
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
class DefaultLanguageChecker(
    private val supportedLanguages: List<String>,
    private val languageDetector: ILanguageDetector
) : LanguageChecker {

    override fun check(instance: PostInstance): Mono<PostInstance> {
        return languageDetector
            .detect(instance)
            .filter { isLanguageSupported(it.language) }
            .switchIfEmpty(
                Mono.error(
                    ClickBaitReadServiceException(
                        "The target language is currently not supported",
                        HttpStatus.BAD_REQUEST
                    )
                )
            )
    }

    private fun isLanguageSupported(language: String): Boolean {
        return supportedLanguages.contains(language)
    }
}