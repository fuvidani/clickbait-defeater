package com.clickbait.defeater.clickbaitservice.read.service

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.clickbait.defeater.clickbaitservice.read.service.language.detector.ILanguageDetector
import com.clickbait.defeater.clickbaitservice.read.service.score.IScoreService
import com.clickbait.defeater.clickbaitservice.read.service.score.cache.IScoreCache
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.io.IOException

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
class ClickBaitReadServiceTest {

    private lateinit var clickBaitReadService: IClickBaitReadService
    @MockBean
    private lateinit var scoreService: IScoreService
    @MockBean
    private lateinit var scoreCache: IScoreCache
    @MockBean
    private lateinit var languageDetector: ILanguageDetector
    private val examplePost =
        PostInstance("url", postText = listOf("You won't believe what Ronaldo did during his press conference"))

    @Before
    fun setUp() {
        clickBaitReadService = ClickBaitReadService(scoreService, scoreCache, languageDetector)
        Mockito.`when`(scoreCache.put(any(ClickBaitScore::class.java))).thenReturn(Mono.just(true))
    }

    @Test
    fun `test scorePostInstance with un-cached score, should score, put result in cache and return`() {
        val expectedScore = ClickBaitScore(examplePost.id, 0.87, "en")

        Mockito.`when`(scoreCache.tryAndGet(examplePost)).thenReturn(Mono.empty())
        Mockito.`when`(languageDetector.detect(examplePost)).thenReturn(Mono.just(examplePost.withLanguage("en")))
        Mockito.`when`(scoreService.scorePostInstance(examplePost.withLanguage("en")))
            .thenReturn(Mono.just(expectedScore))

        val publisher = clickBaitReadService.scorePostInstance(examplePost)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedScore)
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(scoreCache).tryAndGet(examplePost)
        Mockito.verify(scoreCache).put(expectedScore)
        Mockito.verify(languageDetector).detect(examplePost)
        Mockito.verify(scoreService).scorePostInstance(examplePost.withLanguage("en"))
    }

    @Test
    fun `test scorePostInstance with cached score, should immediately return it`() {
        val expectedScore = ClickBaitScore(examplePost.id, 0.87, "en")

        Mockito.`when`(scoreCache.tryAndGet(examplePost)).thenReturn(Mono.just(expectedScore))

        val publisher = clickBaitReadService.scorePostInstance(examplePost)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedScore)
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(scoreCache).tryAndGet(examplePost)
        Mockito.verify(scoreCache, never()).put(any(ClickBaitScore::class.java))
        Mockito.verify(languageDetector, never()).detect(any(PostInstance::class.java))
        Mockito.verify(scoreService, never()).scorePostInstance(any(PostInstance::class.java))
    }

    @Test
    fun `test scorePostInstance with mocked error in scoreService, should propagate it upstream`() {
        Mockito.`when`(scoreCache.tryAndGet(examplePost)).thenReturn(Mono.empty())
        Mockito.`when`(languageDetector.detect(examplePost)).thenReturn(Mono.just(examplePost.withLanguage("en")))
        Mockito.`when`(scoreService.scorePostInstance(examplePost.withLanguage("en")))
            .thenReturn(Mono.error(IOException("Something happened")))

        val publisher = clickBaitReadService.scorePostInstance(examplePost)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectError(IOException::class.java)
            .log()
            .verify()

        Mockito.verify(scoreCache).tryAndGet(examplePost)
        Mockito.verify(scoreCache, never()).put(any(ClickBaitScore::class.java))
        Mockito.verify(languageDetector).detect(examplePost)
        Mockito.verify(scoreService).scorePostInstance(examplePost.withLanguage("en"))
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}