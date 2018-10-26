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
        val webPage = WebPage("redirectUrl", "title")
        val expectedContentWrapper = ContentWrapper(webPage.url, webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(WebPageSource(webPage.url, webPage.url, loadHtmlFromResources("test_html.html"))))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(any(ContentWrapper::class.java))).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.get()
            .uri { it.queryParam("url", webPage.url)
                    .queryParam("title", webPage.title)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
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
        val webPage = WebPage("redirectUrl", "title")
        val expectedContentWrapper = ContentWrapper(webPage.url, webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(WebPageSource(webPage.url, webPage.url, loadHtmlFromResources("test_html.html"))))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(any(ContentWrapper::class.java))).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.get()
            .uri { it.queryParam("url", webPage.url)
                .queryParam("title", webPage.title)
                .build()
            }
            .accept(MediaType.APPLICATION_STREAM_JSON)
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
        val webPage = WebPage("redirectUrl", "title")
        val expectedContentWrapper = ContentWrapper(webPage.url, webPage.url, expectedListOfContentsOfTestHtml())

        Mockito.`when`(htmlProvider.get(webPage)).thenReturn(Mono.just(WebPageSource(webPage.url, webPage.url, loadHtmlFromResources("test_html.html"))))
        Mockito.`when`(redisOperations.get(webPage.url)).thenReturn(Mono.empty())
        Mockito.`when`(repository.findById(webPage.url)).thenReturn(Mono.just(expectedContentWrapper))

        val publisher = client.get()
            .uri { it.queryParam("url", webPage.url)
                .queryParam("title", webPage.title)
                .build()
            }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .returnResult(ContentWrapper::class.java)

        StepVerifier.create(publisher.responseBody)
            .expectSubscription()
            .expectNext(expectedContentWrapper)
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
}