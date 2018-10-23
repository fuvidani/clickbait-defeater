package com.clickbait.defeater.clickbaitservice.read.web

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.read.ClickBaitServiceReadApplication
import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.model.withLanguage
import com.clickbait.defeater.clickbaitservice.read.service.score.client.ScoreServiceClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
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
@SpringBootTest(value = ["application.yml"], classes = [ClickBaitServiceReadApplication::class])
class ClickBaitServiceReadDocumentationTest {

    @Rule
    @JvmField
    final val restDocumentation = JUnitRestDocumentation()
    @Autowired
    private lateinit var clickBaitReadController: ClickBaitReadController
    @MockBean
    private lateinit var scoreServiceClient: ScoreServiceClient
    @MockBean
    private lateinit var redisValueOperations: ReactiveValueOperations<String, ClickBaitScore>
    private lateinit var client: WebTestClient

    @Before
    fun setUp() {
        client = WebTestClient.bindToController(clickBaitReadController)
            .configureClient()
            .baseUrl("http://clickbait-defeater.com/clickbait")
            .filter(documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint()))
            .build()
        }

    @Test
    fun `test score valid minimal post instance, which returns valid ClickBaitScore object`() {
        val postInstance = PostInstance("http://url-of-article.com", postText = listOf("You won't believe what Ronaldo did during his press conference"))
        Mockito.`when`(redisValueOperations.get(postInstance.id)).thenReturn(Mono.empty())
        Mockito.`when`(scoreServiceClient.scorePostInstance(postInstance.withLanguage("en"))).thenReturn(Mono.just(ClickBaitScore(postInstance.id, 0.75)))
        Mockito.`when`(redisValueOperations.set(postInstance.id, ClickBaitScore(postInstance.id, 0.75))).thenReturn(Mono.just(true))

        client.post()
            .uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(postInstance), PostInstance::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(postInstance.id)
            .jsonPath("$.clickbaitScore").isEqualTo(0.75)
            .consumeWith(
                document(
                    "score-valid-post-minimal",
                    requestFields(
                        fieldWithPath("id")
                            .type(JsonFieldType.STRING)
                            .description("Unique identifier of the social media post instance."),
                        fieldWithPath("language")
                            .type(JsonFieldType.STRING)
                            .description("Optional language of the post instance's content or \"unknown\" otherwise.")
                            .optional(),
                        fieldWithPath("postText")
                            .type(JsonFieldType.ARRAY)
                            .description("An array/list of strings as the content of the post. E.g. if the post consists of 5 sentences, then the array may contain these 5 as elements."),
                        fieldWithPath("postTimestamp")
                            .type(JsonFieldType.STRING)
                            .description("Optional timestamp of the post instance's publication.")
                            .optional(),
                        fieldWithPath("postMedia")
                            .type(JsonFieldType.ARRAY)
                            .description("Optional array/list of strings, where each string refers to the identifier/location of a media element (image) connected to this post instance. May be empty.")
                            .optional(),
                        fieldWithPath("targetTitle")
                            .type(JsonFieldType.STRING)
                            .description("Optional title of the targeted external content. This is not to be confused with the `postText`")
                            .optional(),
                        fieldWithPath("targetDescription")
                            .type(JsonFieldType.STRING)
                            .description("Optional description of the targeted external content, usually found in the metadata.")
                            .optional(),
                        fieldWithPath("targetKeywords")
                            .type(JsonFieldType.STRING)
                            .description("Optional comma-separated keywords corresponding to the targeted external content.")
                            .optional(),
                        fieldWithPath("targetParagraphs")
                            .type(JsonFieldType.ARRAY)
                            .description("Optional array/list of strings, where each string refers to a paragraph in the targeted external content. May be empty.")
                            .optional()
                    ),
                    responseFields(
                        fieldWithPath("id")
                            .type(JsonFieldType.STRING)
                            .description("Unique identifier of the corresponding social media post instance this object refers to."),
                        fieldWithPath("clickbaitScore")
                            .type(JsonFieldType.NUMBER)
                            .description("The determined clickbait score of the corresponding post instance, ranging from 0.0 (no-clickbait) to 1.0 (clickbait)."),
                        fieldWithPath("language")
                            .type(JsonFieldType.STRING)
                            .description("Language of the corresponding post instance's content. Currently defaults to \"en\"."),
                        fieldWithPath("message")
                            .type(JsonFieldType.STRING)
                            .description("Additional message field for eventual information propagation.")
                    )
                )
            )
    }

    @Test
    fun `test score valid post instance, which returns valid ClickBaitScore object`() {
        val postInstance = PostInstance("http://www.sportbible.com/football/news-ronaldo-shows-off-18-million-diamond-encrusted-watch-at-presser-20181023?fbclid=IwAR2Q77obnODOi0kTfNtLdJaknsUxhPQ47AqVAGFdhZFssseiRIoDb4oFPcY", postText = listOf("Some watch right there!"), postTimestamp = "2018-10-23T09:52:36.000Z", targetTitle = "Cristiano Ronaldo Shows Off £1.8 Million Diamond-Encrusted Watch At Press Conference", targetDescription = "He flashed his sparkly watch ahead of facing United.", targetKeywords = "Cristiano Ronaldo,Football,Football News,Manchester United,juventus", targetParagraphs = listOf("Ronaldo flashed his diamond-encrusted watch at a pre-match press conference, and it's reportedly worth a staggering £1.85 million.", "Naturally, it's a top of the range watch by designer brand Jacob & Co. But Ronaldo's model was custom made and from the Caviar Tourbillon range.", "The wrist fitted accessory includes 424 (!!) white diamonds.", "How does the watch tick?!"))
        Mockito.`when`(redisValueOperations.get(postInstance.id)).thenReturn(Mono.empty())
        Mockito.`when`(scoreServiceClient.scorePostInstance(postInstance.withLanguage("en"))).thenReturn(Mono.just(ClickBaitScore(postInstance.id, 0.4)))
        Mockito.`when`(redisValueOperations.set(postInstance.id, ClickBaitScore(postInstance.id, 0.4))).thenReturn(Mono.just(true))

        client.post()
            .uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(postInstance), PostInstance::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(postInstance.id)
            .jsonPath("$.clickbaitScore").isEqualTo(0.4)
            .consumeWith(
                document(
                    "score-valid-post-filled",
                    requestFields(
                        fieldWithPath("id")
                            .type(JsonFieldType.STRING)
                            .description("Unique identifier of the social media post instance."),
                        fieldWithPath("language")
                            .type(JsonFieldType.STRING)
                            .description("Optional language of the post instance's content or \"unknown\" otherwise.")
                            .optional(),
                        fieldWithPath("postText")
                            .type(JsonFieldType.ARRAY)
                            .description("An array/list of strings as the content of the post. E.g. if the post consists of 5 sentences, then the array may contain these 5 as elements."),
                        fieldWithPath("postTimestamp")
                            .type(JsonFieldType.STRING)
                            .description("Optional timestamp of the post instance's publication.")
                            .optional(),
                        fieldWithPath("postMedia")
                            .type(JsonFieldType.ARRAY)
                            .description("Optional array/list of strings, where each string refers to the identifier/location of a media element (image) connected to this post instance. May be empty.")
                            .optional(),
                        fieldWithPath("targetTitle")
                            .type(JsonFieldType.STRING)
                            .description("Optional title of the targeted external content. This is not to be confused with the `postText`")
                            .optional(),
                        fieldWithPath("targetDescription")
                            .type(JsonFieldType.STRING)
                            .description("Optional description of the targeted external content, usually found in the metadata.")
                            .optional(),
                        fieldWithPath("targetKeywords")
                            .type(JsonFieldType.STRING)
                            .description("Optional comma-separated keywords corresponding to the targeted external content.")
                            .optional(),
                        fieldWithPath("targetParagraphs")
                            .type(JsonFieldType.ARRAY)
                            .description("Optional array/list of strings, where each string refers to a paragraph in the targeted external content. May be empty.")
                            .optional()
                    ),
                    responseFields(
                        fieldWithPath("id")
                            .type(JsonFieldType.STRING)
                            .description("Unique identifier of the corresponding social media post instance this object refers to."),
                        fieldWithPath("clickbaitScore")
                            .type(JsonFieldType.NUMBER)
                            .description("The determined clickbait score of the corresponding post instance, ranging from 0.0 (no-clickbait) to 1.0 (clickbait)."),
                        fieldWithPath("language")
                            .type(JsonFieldType.STRING)
                            .description("Language of the corresponding post instance's content. Currently defaults to \"en\"."),
                        fieldWithPath("message")
                            .type(JsonFieldType.STRING)
                            .description("Additional message field for eventual information propagation.")
                    )
                )
            )
    }

    @Test
    fun `test score post instance with unsupported language, should return error`() {
        val postInstance = PostInstance("http://url-of-article.com", postText = listOf("Das hätte ich persönlich nie geglaubt...", "Diese 5 Dinge brauchen Sie, um jünger zu werden."))
        Mockito.`when`(redisValueOperations.get(postInstance.id)).thenReturn(Mono.empty())

        client.post()
            .uri("/score")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(postInstance), PostInstance::class.java)
            .exchange()
            .expectStatus().isBadRequest

            // .jsonPath("$.error").isEqualTo("Bad Request")
            // .jsonPath("$.message").isEqualTo("The target language is currently not supported")
            /*.consumeWith(
                document(
                    "score-valid-post-unsupported",
                    requestFields(
                        fieldWithPath("id")
                            .type(JsonFieldType.STRING)
                            .description("Unique identifier of the social media post instance."),
                        fieldWithPath("language")
                            .type(JsonFieldType.STRING)
                            .description("Language of the post instance's content or \"unknown\" otherwise.")
                            .optional(),
                        fieldWithPath("postText")
                            .type(JsonFieldType.ARRAY)
                            .description("An array/list of strings as the content of the post. E.g. if the post consists of 5 sentences, then the array may contain these 5 as elements."),
                        fieldWithPath("postTimestamp")
                            .type(JsonFieldType.STRING)
                            .description("Timestamp of the post instance's publication.")
                            .optional(),
                        fieldWithPath("postMedia")
                            .type(JsonFieldType.ARRAY)
                            .description("An array/list of strings, where each string refers to the identifier/location of a media element (image) connected to this post instance. May be empty.")
                            .optional(),
                        fieldWithPath("targetTitle")
                            .type(JsonFieldType.STRING)
                            .description("The title of the targeted external content. This is not to be confused with the `postText`")
                            .optional(),
                        fieldWithPath("targetDescription")
                            .type(JsonFieldType.STRING)
                            .description("The description of the targeted external content, usually found in the metadata.")
                            .optional(),
                        fieldWithPath("targetKeywords")
                            .type(JsonFieldType.STRING)
                            .description("Comma-separated keywords corresponding to the targeted external content.")
                            .optional(),
                        fieldWithPath("targetParagraphs")
                            .type(JsonFieldType.ARRAY)
                            .description("An array/list of strings, where each string refers to a paragraph in the targeted external content. May be empty.")
                            .optional()
                    ),
                    responseFields(
                        fieldWithPath("error")
                            .type(JsonFieldType.STRING)
                            .description("The error name corresponding to the HTTP status code."),
                        fieldWithPath("message")
                            .type(JsonFieldType.STRING)
                            .description("Textual description of the error."),
                        fieldWithPath("path")
                            .type(JsonFieldType.STRING)
                            .description("The request-URL path"),
                        fieldWithPath("status")
                            .type(JsonFieldType.NUMBER)
                            .description("The HTTP status code of the response"),
                        fieldWithPath("timestamp")
                            .type(JsonFieldType.STRING)
                            .description("Timestamp of the error")
                    )
                )
            )*/
    }
}