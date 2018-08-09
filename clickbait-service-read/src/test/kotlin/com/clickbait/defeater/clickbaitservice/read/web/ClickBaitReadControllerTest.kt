package com.clickbait.defeater.clickbaitservice.read.web

import com.clickbait.defeater.clickbaitservice.read.ClickBaitServiceReadApplication
import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.clickbait.defeater.clickbaitservice.read.service.score.client.IScoreServiceClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
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
@SpringBootTest(value = ["application.yml"], classes = [ClickBaitServiceReadApplication::class])
class ClickBaitReadControllerTest {

    @Autowired
    private lateinit var clickBaitReadController: ClickBaitReadController
    @MockBean
    private lateinit var scoreServiceClient: IScoreServiceClient
    @MockBean
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, ClickBaitScore>
    @MockBean
    private lateinit var redisValueOperations: ReactiveValueOperations<String, ClickBaitScore>
    private lateinit var client: WebTestClient
    private val examplePost =
        PostInstance("url", postText = listOf("You won't believe what Ronaldo did during his press conference"))
    private val expectedClickBaitScore = ClickBaitScore(examplePost.id, 0.88)

    @Before
    fun setUp() {
        client = WebTestClient.bindToController(clickBaitReadController)
            .configureClient()
            .baseUrl("http://clickbait-defeater.com/clickbait")
            .build()
    }

    @Test
    fun `test scoreMediaPost with un-cached result, should return clickBait score`() {
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(redisValueOperations)
        Mockito.`when`(redisValueOperations.get(examplePost.id)).thenReturn(Mono.empty())
        Mockito.`when`(redisValueOperations.set(examplePost.id, expectedClickBaitScore)).thenReturn(Mono.just(true))
        Mockito.`when`(scoreServiceClient.scorePostInstance(examplePost.withLanguage("en")))
            .thenReturn(Mono.just(expectedClickBaitScore))

        val publisher = client.post().uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(examplePost), PostInstance::class.java)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitScore::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(expectedClickBaitScore)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `test scoreMediaPost with cached result, should return cached score immediately`() {
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(redisValueOperations)
        Mockito.`when`(redisValueOperations.get(examplePost.id)).thenReturn(Mono.just(expectedClickBaitScore))

        val publisher = client.post().uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(examplePost), PostInstance::class.java)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitScore::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(expectedClickBaitScore)
            .expectComplete()
            .log()
            .verify()
    }
}