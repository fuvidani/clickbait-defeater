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

package com.clickbait.defeater.clickbaitservice.read.service.score

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import com.clickbait.defeater.clickbaitservice.read.service.score.client.ScoreServiceClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
class ScoreServiceTest {

    private lateinit var scoreService: ScoreService
    @MockBean
    private lateinit var scoreServiceClient: ScoreServiceClient

    @Before
    fun setUp() {
        scoreService = DefaultScoreService(scoreServiceClient)
    }

    @Test
    fun `test scorePostInstance, should return mocked clickbait score`() {
        val expectedScore = ClickBaitScore("url", 42.0, "es")
        Mockito.`when`(scoreServiceClient.scorePostInstance(any(PostInstance::class.java)))
            .thenReturn(Mono.just(ClickBaitScore("url", 42.0)))

        val publisher = scoreService.scorePostInstance(PostInstance("url", "es", listOf("Hola!")))
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedScore)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `test scorePostInstance, mocking network error, should map to correct Exception event`() {
        Mockito.`when`(scoreServiceClient.scorePostInstance(any(PostInstance::class.java)))
            .thenReturn(Mono.error(IOException("Could not reach service")))

        val publisher = scoreService.scorePostInstance(PostInstance("url", "es", listOf("Hola!")))
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectError(ClickBaitReadServiceException::class.java)
            .log()
            .verify()
    }

    @Test
    fun `test scorePostInstance, mocking empty result, should return empty Mono`() {
        Mockito.`when`(scoreServiceClient.scorePostInstance(any(PostInstance::class.java)))
            .thenReturn(Mono.empty())

        val publisher = scoreService.scorePostInstance(PostInstance("url", "es", listOf("Hola!")))
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}