package com.clickbait.defeater.clickbaitservice.update.web

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.ClickBaitServiceUpdateApplication
import com.clickbait.defeater.clickbaitservice.update.config.DbIntegrationTest
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeStrategies
import reactor.core.publisher.Mono

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
class ClickBaitUpdateControllerDocumentationTest {

    @Rule
    @JvmField
    final val restDocumentation = JUnitRestDocumentation()
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
            .filter(
                WebTestClientRestDocumentation.documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(Preprocessors.prettyPrint())
                    .withResponseDefaults(Preprocessors.prettyPrint()))
            .build()
        mongoTemplate.dropCollection("posts")
        mongoTemplate.dropCollection("votes")
    }

    @Test
    fun `test submit vote, should execute normally`() {
        val vote = ClickBaitVote("uniqueUserId", "https://url_of_article.com/", 0.66)
        client.post().uri("/vote")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(vote), ClickBaitVote::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(
                document(
                    "submit-vote",
                    requestFields(
                        fieldWithPath("userId")
                            .type(JsonFieldType.STRING)
                            .description("Unique user ID of the voter (client)"),
                        fieldWithPath("url")
                            .type(JsonFieldType.STRING)
                            .description("The URL of the article for which the vote is submitted"),
                        fieldWithPath("vote")
                            .type(JsonFieldType.NUMBER)
                            .description("The actual vote between 0.0 (no-clickbait) and 1.0 (clickbait) according to the voter"),
                        fieldWithPath("postText")
                            .type(JsonFieldType.ARRAY)
                            .description("Optional array of strings representing the text which this article has been posted with. Defaults to an empty array."),
                        fieldWithPath("lastUpdate")
                            .type(JsonFieldType.STRING)
                            .description("The date and time when this particular vote has been submitted or updated. This field must not be provided by the client, it is set by the server (and is overwritten in case you do provide it)")
                    )
                )
            )
    }

    @Test
    fun `test retrieve non-existing vote, should return 204 NO_CONTENT`() {
        client.get()
            .uri {
                it
                    .path("/vote")
                    .queryParam("userId", "uniqueUserId")
                    .queryParam("url", "https://some_article_without_votes.com/")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `test retrieve existing vote, should return expected vote`() {
        val vote = ClickBaitVote("uniqueUserId", "https://url_of_article.com/", 0.66)
        client.post().uri("/vote")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(vote), ClickBaitVote::class.java)
            .exchange()
            .expectStatus().isOk

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
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.userId").isEqualTo(vote.userId)
            .jsonPath("$.url").isEqualTo(vote.url)
            .jsonPath("$.vote").isEqualTo(vote.vote)
            .jsonPath("$.postText").exists()
            .jsonPath("$.lastUpdate").isNotEmpty
            .consumeWith(
                document(
                    "get-existing-vote",
                    requestParameters(
                        parameterWithName("userId").description("Unique user ID"),
                        parameterWithName("url").description("URL of particular article")
                    ),
                    responseFields(
                        fieldWithPath("userId")
                            .type(JsonFieldType.STRING)
                            .description("Unique user ID"),
                        fieldWithPath("url")
                            .type(JsonFieldType.STRING)
                            .description("The URL of the article"),
                        fieldWithPath("vote")
                            .type(JsonFieldType.NUMBER)
                            .description("The actual vote between 0.0 (no-clickbait) and 1.0 (clickbait)"),
                        fieldWithPath("postText")
                            .type(JsonFieldType.ARRAY)
                            .description("Array of strings representing the text which this article has been posted with. May be empty."),
                        fieldWithPath("lastUpdate")
                            .type(JsonFieldType.NUMBER)
                            .description("The date and time when this particular vote has been submitted or updated. The time zone aligns with the server and not with the client.")
                    )
                )
            )
    }

    @Test
    fun `test retrieve all of a particular user's votes paged, should return correctly paged votes`() {
        controller.submitVote(ClickBaitVote("1234", "someUrl_1", 0.2))
            .concatWith(controller.submitVote(ClickBaitVote("1234", "someUrl_2", 0.4)))
            .concatWith(controller.submitVote(ClickBaitVote("1234", "someUrl_3", 0.6)))
            .concatWith(controller.submitVote(ClickBaitVote("1234", "someUrl_4", 0.8)))
            .collectList().block()
        client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "1234")
                    .queryParam("page", "0")
                    .queryParam("size", "2")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(
                document(
                    "get-paged-votes",
                    requestParameters(
                        parameterWithName("userId").description("Unique user ID"),
                        parameterWithName("page").description("Optional number of page in case of a paged request. Note that this parameter only works in conjunction with the \"size\" parameter."),
                        parameterWithName("size").description("Optional size of the requested page. Note that this parameter only works in conjunction with the \"page\" parameter.")
                    ),
                    responseFields(
                        fieldWithPath("[].userId")
                            .type(JsonFieldType.STRING)
                            .description("Unique user ID"),
                        fieldWithPath("[].url")
                            .type(JsonFieldType.STRING)
                            .description("The URL of the article"),
                        fieldWithPath("[].vote")
                            .type(JsonFieldType.NUMBER)
                            .description("The actual vote between 0.0 (no-clickbait) and 1.0 (clickbait)"),
                        fieldWithPath("[].postText")
                            .type(JsonFieldType.ARRAY)
                            .description("Array of strings representing the text which this article has been posted with. May be empty."),
                        fieldWithPath("[].lastUpdate")
                            .type(JsonFieldType.NUMBER)
                            .description("The date and time when this particular vote has been submitted or updated. The time zone aligns with the server and not with the client.")
                    )
                )
            )
    }

    @Test
    fun `test retrieve all of a particular user's votes NOT paged, should return correctly NOT paged votes`() {
        controller.submitVote(ClickBaitVote("1234", "someUrl_1", 0.2))
            .concatWith(controller.submitVote(ClickBaitVote("1234", "someUrl_2", 0.4)))
            .concatWith(controller.submitVote(ClickBaitVote("1234", "someUrl_3", 0.6)))
            .concatWith(controller.submitVote(ClickBaitVote("1234", "someUrl_4", 0.8)))
            .collectList().block()
        client.get()
            .uri {
                it
                    .path("/votes")
                    .queryParam("userId", "1234")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(
                document(
                    "get-unpaged-votes",
                    requestParameters(
                        parameterWithName("userId").description("Unique user ID")
                    ),
                    responseFields(
                        fieldWithPath("[].userId")
                            .type(JsonFieldType.STRING)
                            .description("Unique user ID"),
                        fieldWithPath("[].url")
                            .type(JsonFieldType.STRING)
                            .description("The URL of the article"),
                        fieldWithPath("[].vote")
                            .type(JsonFieldType.NUMBER)
                            .description("The actual vote between 0.0 (no-clickbait) and 1.0 (clickbait)"),
                        fieldWithPath("[].postText")
                            .type(JsonFieldType.ARRAY)
                            .description("Array of strings representing the text which this article has been posted with. May be empty."),
                        fieldWithPath("[].lastUpdate")
                            .type(JsonFieldType.NUMBER)
                            .description("The date and time when this particular vote has been submitted or updated. The time zone aligns with the server and not with the client.")
                    )
                )
            )
    }
}