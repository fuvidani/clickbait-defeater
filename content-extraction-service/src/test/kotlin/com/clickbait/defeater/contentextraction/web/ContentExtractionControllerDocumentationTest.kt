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

package com.clickbait.defeater.contentextraction.web

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.ContentExtractionServiceApplication
import com.clickbait.defeater.contentextraction.model.*
import com.clickbait.defeater.contentextraction.persistence.repository.ContentRepository
import com.clickbait.defeater.contentextraction.service.html.HtmlProvider
import org.jsoup.Jsoup
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.io.File
import java.time.Duration

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
class ContentExtractionControllerDocumentationTest {

    @Rule
    @JvmField
    final val restDocumentation = JUnitRestDocumentation()
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
            .filter(
                WebTestClientRestDocumentation.documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(Preprocessors.prettyPrint())
                    .withResponseDefaults(Preprocessors.prettyPrint()))
            .responseTimeout(Duration.ofSeconds(15))
            .build()
    }

    @Test
    fun `test extraction with wrapper response, should return extracted content`() {
        val webPage = WebPage("https://some-webpage-url", "Title")
        val expectedContentWrapper = ContentWrapper(webPage.url, webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(WebPageSource(webPage.url, webPage.url, loadHtmlFromResources("test_html.html"))))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(any(ContentWrapper::class.java))).thenReturn(Mono.just(expectedContentWrapper))

        client.get()
            .uri { it.queryParam("url", webPage.url)
                .queryParam("title", webPage.title)
                .build()
            }
            .accept(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(
                document(
                    "extract-wrapper",
                    requestParameters(
                        parameterWithName("url").description("URL of the webpage that should be scraped. If the URL is statically redirected, it is followed to the source"),
                        parameterWithName("title").optional().description("An optional title parameter, in case the client already possesses the target webpage's title.")
                    ),
                    responseFields(
                        fieldWithPath("redirectUrl")
                            .type(JsonFieldType.STRING)
                            .description("The provided URL which is generally considered to be a redirect one. May be equal to sourceUrl."),
                        fieldWithPath("sourceUrl")
                            .type(JsonFieldType.STRING)
                            .description("The actual URL of the target page. The redirectUrl and sourceUrl attributes only differ if the provided URL was a redirect one."),
                        fieldWithPath("contents")
                            .type(JsonFieldType.ARRAY)
                            .description("An array of different extracted contents. The array can be of arbitrary length."),
                        fieldWithPath("contents[].contentType")
                            .type(JsonFieldType.STRING)
                            .description("The type of content, which can be TEXT, MEDIA, SOCIAL_MEDIA, META_DATA and HTML"),
                        fieldWithPath("contents[].text").optional()
                            .type(JsonFieldType.STRING)
                            .description("A textual content like a word, sentence or paragraph."),
                        fieldWithPath("contents[].data").optional()
                            .type(JsonFieldType.STRING)
                            .description("This attribute occurs in META_DATA content types. For language meta data, this attribute refers to the language of the page."),
                        fieldWithPath("contents[].type").optional()
                            .type(JsonFieldType.STRING)
                            .description("Type attribute used to differentiate between META_DATA, MEDIA and SOCIAL_MEDIA types."),
                        fieldWithPath("contents[].src").optional()
                            .type(JsonFieldType.STRING)
                            .description("Usually an URL pointing to the source of a particular MEDIA or SOCIAL_MEDIA content")
                    )
                )
            )
    }

    private fun loadHtmlFromResources(filename: String): String {
        return Jsoup.parse(File(javaClass.classLoader.getResource(filename).file), Charsets.UTF_8.name()).html()
    }

    private fun expectedListOfContentsOfTestHtml(): List<Content> {
        return listOf(
            TextContent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."),
            TextContent("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua."),
            TextContent("At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."),
            MetaDataContent(MetaDataType.LANGUAGE, "en"),
            MetaDataContent(MetaDataType.KEYWORDS, "Some, keywords, helping, achieve, semantic, web"),
            MetaDataContent(MetaDataType.TITLE, "Test HTML file"),
            MetaDataContent(MetaDataType.DESCRIPTION, "This is an HTML file for testing various content extractors. It's content may be extended when new features, extractors get added."),
            MetaDataContent(MetaDataType.TIMESTAMP, "2018-08-15T10:45:11+01:00"),
            MetaDataContent(MetaDataType.IMAGE, "https://www.pexels.com/photo/laptop-calendar-and-books-908298/"),
            MetaDataContent(MetaDataType.VIDEO, "https://some-video.com/"),
            MediaContent(MediaType.VIDEO, "http://some-stream.com"),
            MediaContent(MediaType.VIDEO, "http://some-stream.com/2"),
            MediaContent(MediaType.VIDEO, "http://some-stream.com/3")
        )
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}