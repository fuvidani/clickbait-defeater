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

package com.clickbait.defeater.clickbaitservice.update.service

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.persistence.ClickBaitVoteRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.PostInstanceService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

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
class ClickBaitVoteServiceTest {

    private lateinit var clickBaitVoteService: ClickBaitVoteService
    @MockBean
    private lateinit var voteRepository: ClickBaitVoteRepository
    @MockBean
    private lateinit var postInstanceService: PostInstanceService

    @Before
    fun setUp() {
        clickBaitVoteService = DefaultClickBaitVoteService(voteRepository, postInstanceService)
    }

    @Test
    fun `GIVEN a valid vote, THEN service's submitVote() successfully processes it`() {
        val vote = ClickBaitVote("userId", "someUrl", 0.7, listOf("Hello World"))
        Mockito.`when`(voteRepository.save(any(ClickBaitVoteEntity::class.java))).thenReturn(Mono.just(vote.toEntity()))
        Mockito.`when`(postInstanceService.ensurePersistedPostInstance(vote)).thenReturn(Mono.delay(Duration.ofSeconds(1)).map { true })

        val publisher = clickBaitVoteService.submitVote(vote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNoEvent(Duration.ofMillis(500))
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(voteRepository).save(any(ClickBaitVoteEntity::class.java))
        Mockito.verify(postInstanceService).ensurePersistedPostInstance(vote)
    }

    @Test
    fun `GIVEN a userId AND an URL for which a vote already exists, THEN service's findVote() returns it`() {
        val vote = ClickBaitVote("userId", "someUrl", 0.7, listOf("Hello World"))
        val timeOfVote = Instant.now()
        val entity = vote.toEntity(timeOfVote)
        Mockito.`when`(voteRepository.findById(entity.id)).thenReturn(Mono.just(entity))
        val expectedVote = ClickBaitVote(vote.userId, vote.url, vote.vote, lastUpdate = ZonedDateTime.ofInstant(timeOfVote,
            ZoneId.of(SERVICE_ZONE_ID)))

        val publisher = clickBaitVoteService.findVote(vote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedVote)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId AND an URL for which NO vote exist, THEN service's findVote() returns empty Mono`() {
        val vote = ClickBaitVote("userId", "someUrl", 0.7, listOf("Hello World"))
        Mockito.`when`(voteRepository.findById(ClickBaitVoteKey(vote.userId, vote.url))).thenReturn(Mono.empty())

        val publisher = clickBaitVoteService.findVote(vote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which at least 1 vote exist AND a valid Pageable object, THEN findAllUserVotes() returns paged votes of user`() {
        val timeOfVote = Instant.now()
        val vote = ClickBaitVote("userId", "someUrl", 0.7, lastUpdate = ZonedDateTime.ofInstant(timeOfVote, ZoneId.of(SERVICE_ZONE_ID)))
        val vote2 = ClickBaitVote("userId", "someUrl2", 0.7, lastUpdate = ZonedDateTime.ofInstant(timeOfVote, ZoneId.of(SERVICE_ZONE_ID)))
        val vote3 = ClickBaitVote("userId", "someUrl3", 0.7, lastUpdate = ZonedDateTime.ofInstant(timeOfVote, ZoneId.of(SERVICE_ZONE_ID)))
        val votes = listOf(vote, vote2, vote3)
        val voteEntities = votes.map { it.toEntity(timeOfVote) }
        val pageable = PageRequest.of(0, 10)
        Mockito.`when`(voteRepository.findByIdUserId(vote.userId, pageable)).thenReturn(Flux.fromIterable(voteEntities))

        val publisher = clickBaitVoteService.findAllUserVotes("userId", pageable)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNextSequence(votes)
            .expectComplete()
            .log()
            .verify()
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}