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