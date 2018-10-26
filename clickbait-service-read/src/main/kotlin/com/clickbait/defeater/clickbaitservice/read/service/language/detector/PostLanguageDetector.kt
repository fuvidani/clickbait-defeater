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