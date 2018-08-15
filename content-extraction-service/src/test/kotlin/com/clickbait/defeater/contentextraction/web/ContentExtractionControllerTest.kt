package com.clickbait.defeater.contentextraction.web

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.ContentExtractionServiceApplication
import com.clickbait.defeater.contentextraction.model.*
import com.clickbait.defeater.contentextraction.persistence.repository.ContentRepository
import com.clickbait.defeater.contentextraction.service.html.HtmlProvider
import org.jsoup.Jsoup
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
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.io.File

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
@SpringBootTest(value = ["application.yml",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration"],
    classes = [ContentExtractionServiceApplication::class])
class ContentExtractionControllerTest {

    @Autowired
    private lateinit var controller: ContentExtractionController
    @MockBean
    private lateinit var redisOperations: ReactiveValueOperations<String, ContentWrapper>
    @MockBean
    private lateinit var repository: ContentRepository
    @MockBean
    private lateinit var htmlProvider: HtmlProvider
    private lateinit var client: WebTestClient

    @Before
    fun setUp() {
        Mockito.`when`(redisOperations.set(any(String::class.java), any(ContentWrapper::class.java))).thenReturn(Mono.just(true))
        client = WebTestClient.bindToController(controller)
            .configureClient()
            .baseUrl("http://clickbait-defeater.com/content")
            .build()
    }

    @Test
    fun `Given a valid AND un-scraped URL, controller returns scraped content AND persists it in data store`() {
        val webPage = WebPage("url", "title")
        val expectedContentWrapper = ContentWrapper(webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(loadHtmlFromResources("test_html.html")))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(expectedContentWrapper)).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.post().uri("/extract")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(webPage), WebPage::class.java)
            .exchange()
            .expectStatus().isOk
            .returnResult(ContentWrapper::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(expectedContentWrapper)
            .verifyComplete()
    }

    @Test
    fun `Given a valid AND un-scraped URL, controller streams scraped content AND persists it in data store`() {
        val webPage = WebPage("url", "title")
        val expectedContentWrapper = ContentWrapper(webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(loadHtmlFromResources("test_html.html")))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(expectedContentWrapper)).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.post().uri("/extract")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .body(Mono.just(webPage), WebPage::class.java)
            .exchange()
            .expectStatus().isOk
            .returnResult(Content::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNextSequence(expectedContentWrapper.contents)
            .verifyComplete()
    }

    @Test
    fun `Given a valid AND already scraped URL, controller returns stored content`() {
        val webPage = WebPage("url", "title")
        val expectedContentWrapper = ContentWrapper(webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(loadHtmlFromResources("test_html.html")))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.post().uri("/extract")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(webPage), WebPage::class.java)
            .exchange()
            .expectStatus().isOk
            .returnResult(ContentWrapper::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(expectedContentWrapper)
            .verifyComplete()
    }

    @Test
    fun `Given an incomplete PostInstance, THEN controller scrapes missing content AND returns complete PostInstance`() {
        val post = PostInstance("url", postText = listOf("This is the text that this instance was posted with"))
        val webPage = WebPage(post.id, "")
        val expectedContentWrapper = ContentWrapper(webPage.url, expectedListOfContentsOfTestHtml())
        val expectedPostInstance = expectedCompletePostInstanceOfTestHtml(post.id, post.postText)

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(loadHtmlFromResources("test_html.html")))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(expectedContentWrapper)).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.post().uri("/completePost")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(post), PostInstance::class.java)
            .exchange()
            .expectStatus().isOk
            .returnResult(PostInstance::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(expectedPostInstance)
            .verifyComplete()
    }

    private fun loadHtmlFromResources(filename: String): String {
        return Jsoup.parse(File(javaClass.classLoader.getResource(filename).file), Charsets.UTF_8.name()).html()
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun expectedListOfContentsOfTestHtml(): List<Content> {
        return listOf(TextContent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."),
            TextContent("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua."),
            TextContent("At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."),
            MetaDataContent(MetaDataType.LANGUAGE, "en"),
            MetaDataContent(MetaDataType.KEYWORDS, "Some, keywords, helping, achieve, semantic, web"),
            MetaDataContent(MetaDataType.TITLE, "Test HTML file"),
            MetaDataContent(MetaDataType.DESCRIPTION, "This is an HTML file for testing various content extractors. It's content may be extended when new features, extractors get added."),
            MetaDataContent(MetaDataType.TIMESTAMP, "2018-08-15T10:45:11+01:00"),
            MetaDataContent(MetaDataType.IMAGE, "https://www.pexels.com/photo/laptop-calendar-and-books-908298/"),
            MetaDataContent(MetaDataType.VIDEO, "https://some-video.com/"),
            MediaContent(com.clickbait.defeater.contentextraction.model.MediaType.VIDEO, "http://some-stream.com"),
            MediaContent(com.clickbait.defeater.contentextraction.model.MediaType.VIDEO, "http://some-stream.com/2"),
            MediaContent(com.clickbait.defeater.contentextraction.model.MediaType.VIDEO, "http://some-stream.com/3"))
    }

    private fun expectedCompletePostInstanceOfTestHtml(url: String, postText: List<String>): PostInstance {
        val contents = expectedListOfContentsOfTestHtml()
        val textContents: List<TextContent> = contents.filter { it is TextContent }.map { it as TextContent }
        return PostInstance(url, "en", postText,
            "2018-08-15T10:45:11+01:00",
            emptyList(),
            "Test HTML file",
            "This is an HTML file for testing various content extractors. It's content may be extended when new features, extractors get added.",
            "Some, keywords, helping, achieve, semantic, web",
            listOf(textContents[0].text, textContents[1].text, textContents[2].text))
    }
}