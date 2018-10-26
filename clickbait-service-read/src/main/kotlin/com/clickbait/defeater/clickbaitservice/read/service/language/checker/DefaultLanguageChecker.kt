/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.clickbaitservice.read.service.language.checker

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import com.clickbait.defeater.clickbaitservice.read.service.language.detector.LanguageDetector
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [LanguageChecker] interface.
 * Utilizes the [LanguageDetector] interface and a list of currently supported
 * languages provided by instantiation.
 *
 * @property supportedLanguages list of ISO-639-1 language codes that this checker
 * currently accepts
 * @property languageDetector implementation of the [LanguageDetector] interface for
 * detecting the most probable language of a certain string
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DefaultLanguageChecker(
    private val supportedLanguages: List<String>,
    private val languageDetector: LanguageDetector
) : LanguageChecker {

    /**
     * Performs certain checks on the provided [PostInstance] object
     * and either emits the `instance` or an error as a result.
     *
     * @param instance a valid social media post instance
     * @return a Mono emitting the provided `instance` if all checks
     * passed, otherwise an error
     */
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