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
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import com.clickbait.defeater.clickbaitservice.read.service.language.detector.LanguageDetector
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringRunner::class)
class LanguageCheckerTest {

    private lateinit var languageChecker: LanguageChecker
    @MockBean
    private lateinit var languageDetector: LanguageDetector

    @Before
    fun setUp() {
        languageChecker = DefaultLanguageChecker(listOf("en"), languageDetector)
    }

    @Test
    fun `GIVEN  a post instance with supported language, THEN checker returns instance with extended language field`() {
        val post = PostInstance("id", postText = listOf("See the $1.5 million Kickstarter - only 1 day left"))
        Mockito.`when`(languageDetector.detect(post)).thenReturn(Mono.just(post.withLanguage("en")))

        StepVerifier.create(languageChecker.check(post))
            .expectSubscription()
            .expectNext(post.withLanguage("en"))
            .expectComplete()
            .verify()
    }

    @Test
    fun `GIVEN a post instance with unsupported language, THEN checker returns correct error type`() {
        val post = PostInstance("id", postText = listOf("Der Artikel wird dem Käufer unter Ausschuss jeder Rechts- und Sachmängel verkauft."))
        Mockito.`when`(languageDetector.detect(post)).thenReturn(Mono.just(post.withLanguage("de")))

        StepVerifier.create(languageChecker.check(post))
            .expectSubscription()
            .expectError(ClickBaitReadServiceException::class.java)
            .log()
            .verify()
    }
}