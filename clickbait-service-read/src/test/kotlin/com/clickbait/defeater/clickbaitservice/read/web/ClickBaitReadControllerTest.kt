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

package com.clickbait.defeater.clickbaitservice.read.web

import com.clickbait.defeater.clickbaitservice.read.ClickBaitServiceReadApplication
import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.clickbait.defeater.clickbaitservice.read.service.score.client.ScoreServiceClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
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
    private lateinit var scoreServiceClient: ScoreServiceClient
    @MockBean
    private lateinit var redisValueOperations: ReactiveValueOperations<String, ClickBaitScore>
    private lateinit var client: WebTestClient
    private val testPostSupported =
        PostInstance("url", postText = listOf("You won't believe what Ronaldo did during his press conference"))
    private val testPostUnSupported =
        PostInstance("url", postText = listOf("Wegen der neuen Gesetzesbestimmungen erfolgt der Verkauf unter Ausschluss jeglicher Gewährleistung, Garantie und Rücknahme. Da es sich um einen Privatverkauf handelt, kann ich keine Garantie nach neuem EU-Recht übernehmen."))
    private val expectedClickBaitScore = ClickBaitScore(testPostSupported.id, 0.88)

    @Before
    fun setUp() {
        client = WebTestClient.bindToController(clickBaitReadController)
            .configureClient()
            .baseUrl("http://clickbait-defeater.com/clickbait")
            .build()
    }

    @Test
    fun `test scoreMediaPost with un-cached result, should return clickBait score`() {
        Mockito.`when`(redisValueOperations.get(testPostSupported.id)).thenReturn(Mono.empty())
        Mockito.`when`(redisValueOperations.set(testPostSupported.id, expectedClickBaitScore)).thenReturn(Mono.just(true))
        Mockito.`when`(scoreServiceClient.scorePostInstance(testPostSupported.withLanguage("en")))
            .thenReturn(Mono.just(expectedClickBaitScore))

        val publisher = client.post().uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(testPostSupported), PostInstance::class.java)
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
        Mockito.`when`(redisValueOperations.get(testPostSupported.id)).thenReturn(Mono.just(expectedClickBaitScore))

        val publisher = client.post().uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(testPostSupported), PostInstance::class.java)
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
    fun `test scoreMediaPost with unsupported language, should return correct error message`() {
        Mockito.`when`(redisValueOperations.get(testPostSupported.id)).thenReturn(Mono.empty())
        Mockito.`when`(redisValueOperations.set(testPostSupported.id, expectedClickBaitScore)).thenReturn(Mono.just(true))

        client.post().uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(testPostUnSupported), PostInstance::class.java)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody(ResponseStatusException::class.java)
    }
}