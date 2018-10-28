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

package com.clickbait.defeater.clickbaitservice.update.web

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.ClickBaitServiceUpdateApplication
import com.clickbait.defeater.clickbaitservice.update.config.DbIntegrationTest
import com.clickbait.defeater.clickbaitservice.update.model.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies

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
@Category(DbIntegrationTest::class)
@SpringBootTest(value = ["application.yml"], classes = [ClickBaitServiceUpdateApplication::class])
class ClickBaitUpdateControllerTest {

    @Autowired
    private lateinit var controller: ClickBaitUpdateController
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate
    private lateinit var client: WebTestClient
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp() {
        val strategies = ExchangeStrategies
            .builder()
            .codecs { clientDefaultCodecsConfigurer ->
                clientDefaultCodecsConfigurer.defaultCodecs()
                    .jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_STREAM_JSON))
                clientDefaultCodecsConfigurer.defaultCodecs()
                    .jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_STREAM_JSON))
            }.build()
        client = WebTestClient.bindToController(controller)
            .configureClient()
            .exchangeStrategies(strategies)
            .baseUrl("http://clickbait-defeater.com/clickbait")
            .build()
        mongoTemplate.dropCollection("posts")
        mongoTemplate.dropCollection("votes")
    }

    @Test
    fun `GIVEN a new vote, THEN service processes it AND client gets result OK`() {
        val vote = ClickBaitVote("userId", "someUrl", 0.7, listOf("Hello World"))

        client.post().uri("/vote")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(vote), ClickBaitVote::class.java)
            .exchange()
            .expectStatus().isOk

        val votes = mongoTemplate.findAll(ClickBaitVoteEntity::class.java)
        Assert.assertEquals(1, votes.size)
        assertExpectedVote(vote, votes[0])
    }

    @Test
    fun `GIVEN a userId and a URL for which no vote exists yet, THEN GET vote returns 204 NO CONTENT`() {
        val vote = ClickBaitVote(
            "userId",
            "https://l.facebook.com/l.php?u=https%3A%2F%2Fkarriere.mckinsey.de%2Fevent%2Fdigital-campus%3Fpk_campaign%3Ddigitalcampus%26pk_kwd%3Dfacebookadstatisch2&h=AT0eLkKJJDcSbJdJnUX-pud5P0oRvjrjbRsP5L-wFCtNJoB-ZNWMlX_-bpj01M7Enab8GMSf2fdlsS7d0f9-DQmUL5ASZH7kcbyRgFOQYsqrB0ynkDSDXOLQEtcJ3ftgwO3nLTLV0bkh6J9ufRlhFw_p31MBWhZty10-UdhMsH_wwYqhIAxrOShGr2pEn-WaEdyHhT-vAWeJcZ4emwEmUZNoymaa4PyAKcvBvBq01KXGnYcRTI-FrSkiTrZXlPZYBQT2dQugiFNVkkHZBH_x9G3BhQ1kMFK2Sb4xAS59TTup0CGy1aXUT1zATc51WLB0Qc_pRdsQa31MyQTCPqx7aUo5hsqAJEEU58mcLeHmJnwF3lzu97y0LOCXhlJyehCZ9ZTNmtqMTKi_4rAO8aVIu8dI7DOj4ulzsgu0jOVYNUrCCNIXdyJHwQwROGwuRPWdRS8",
            0.7,
            listOf("Hello World")
        )

        client.get()
            .uri {
                it
                    .path("/vote")
                    .queryParam("userId", vote.userId)
                    .queryParam("url", vote.url)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `GIVEN a vote submission followed by a query for the vote, THEN GET vote returns vote as body`() {

        val vote = ClickBaitVote(
            "userId",
            "https://l.facebook.com/l.php?u=https%3A%2F%2Fkarriere.mckinsey.de%2Fevent%2Fdigital-campus%3Fpk_campaign%3Ddigitalcampus%26pk_kwd%3Dfacebookadstatisch2&h=AT0eLkKJJDcSbJdJnUX-pud5P0oRvjrjbRsP5L-wFCtNJoB-ZNWMlX_-bpj01M7Enab8GMSf2fdlsS7d0f9-DQmUL5ASZH7kcbyRgFOQYsqrB0ynkDSDXOLQEtcJ3ftgwO3nLTLV0bkh6J9ufRlhFw_p31MBWhZty10-UdhMsH_wwYqhIAxrOShGr2pEn-WaEdyHhT-vAWeJcZ4emwEmUZNoymaa4PyAKcvBvBq01KXGnYcRTI-FrSkiTrZXlPZYBQT2dQugiFNVkkHZBH_x9G3BhQ1kMFK2Sb4xAS59TTup0CGy1aXUT1zATc51WLB0Qc_pRdsQa31MyQTCPqx7aUo5hsqAJEEU58mcLeHmJnwF3lzu97y0LOCXhlJyehCZ9ZTNmtqMTKi_4rAO8aVIu8dI7DOj4ulzsgu0jOVYNUrCCNIXdyJHwQwROGwuRPWdRS8",
            0.7,
            listOf("Hello World")
        )

        // QUERY VOTE -> SHOULD RETURN NO CONTENT
        client.get()
            .uri {
                it
                    .path("/vote")
                    .queryParam("userId", vote.userId)
                    .queryParam("url", vote.url)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isNoContent

        // SUBMIT VOTE -> SHOULD RETURN OK
        client.post().uri("/vote")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(vote), ClickBaitVote::class.java)
            .exchange()
            .expectStatus().isOk

        // QUERY VOTE -> SHOULD RETURN SUBMITTED VOTE
        val publisher = client.get()
            .uri {
                it
                    .path("/vote")
                    .queryParam("userId", vote.userId)
                    .queryParam("url", vote.url)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextMatches {
                it.userId == vote.userId &&
                        it.url == URLDecoder.decode(vote.url, StandardCharsets.UTF_8.name()) &&
                        it.vote == vote.vote &&
                        it.lastUpdate.isBefore(ZonedDateTime.now()) &&
                it.lastUpdate.zone.id == ZoneId.of(SERVICE_ZONE_ID).id
            }
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN GET votes returns correctly paged votes 1`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextSequence(userAList)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN GET votes returns correctly paged votes 2`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .queryParam("page", "0")
                    .queryParam("size", "2")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(userAList[0])
            .expectNext(userAList[1])
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN GET votes returns correctly paged votes 3`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .queryParam("page", "4")
                    .queryParam("size", "1")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(userAList[4])
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN GET votes returns correctly paged votes 4`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .queryParam("page", "-15")
                    .queryParam("size", "1")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextSequence(userAList)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN GET votes returns correctly paged votes 5`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .queryParam("page", "2")
                    .queryParam("size", "0")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextSequence(userAList)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN GET votes returns correctly paged votes 6`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .queryParam("page", "0")
                    .queryParam("size", "10")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextSequence(userAList)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist AND client requests stream, THEN GET votes streams results unpaged`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "userA")
                    .build()
            }
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus().isOk
            .returnResult(ClickBaitVote::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextSequence(userAList)
            .expectComplete()
            .log()
            .verify()
    }

    private fun assertExpectedVote(reference: ClickBaitVote, entity: ClickBaitVoteEntity) {
        Assert.assertEquals(ClickBaitVoteKey(reference.userId, reference.url), entity.id)
        Assert.assertEquals(reference.vote, entity.vote, 0.0)
    }

    private fun getDummyListOfVotes(userId: String, timeOfInsertion: Instant, vararg urls: String): List<ClickBaitVote> {
        val result = mutableListOf<ClickBaitVote>()
        urls.forEach {
            val vote = ClickBaitVote(userId, it, Random().nextDouble(), emptyList(), timeOfInsertion.atZone(ZoneId.of(
                SERVICE_ZONE_ID)))
            result.add(vote)
        }
        return result
    }

    private fun saveClickBaitVotes(timeOfInsertion: Instant, votes: List<ClickBaitVote>) {
        votes.forEach {
            mongoTemplate.save(it.toEntity(timeOfInsertion), "votes")
        }
    }
}