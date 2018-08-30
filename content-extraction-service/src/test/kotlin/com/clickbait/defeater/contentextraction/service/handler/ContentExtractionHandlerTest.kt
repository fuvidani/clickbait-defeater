package com.clickbait.defeater.contentextraction.service.handler

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.persistence.TestData
import com.clickbait.defeater.contentextraction.service.html.HtmlProvider
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
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
class ContentExtractionHandlerTest {

    private lateinit var handler: ContentExtractionHandler
    @MockBean
    private lateinit var htmlProvider: HtmlProvider
    @MockBean
    private lateinit var extractorChain: ExtractorChain

    @Before
    fun setUp() {
        handler = DefaultContentExtractionHandler(htmlProvider, extractorChain)
    }

    @Test
    fun `Given a valid URL and content behind it, THEN handler returns extracted content`() {
        val page = WebPage("redirectUrl", "title")
        val expectedContent = TestData.getSampleContentWrapper("redirectUrl")
        Mockito.`when`(htmlProvider.get(any(WebPage::class.java))).thenReturn(Mono.just(WebPageSource("redirectUrl", "redirectUrl", "some html data")))
        Mockito.`when`(extractorChain.extract(any(WebPageSource::class.java))).thenReturn(Flux.fromIterable(expectedContent.contents))
        val publisher = handler.extract(page)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedContent)
            .verifyComplete()
    }

    @Test
    fun `Given a valid URL with no usable content, THEN handler returns empty result`() {
        val page = WebPage("redirectUrl", "title")
        Mockito.`when`(htmlProvider.get(any(WebPage::class.java))).thenReturn(Mono.just(WebPageSource("redirectUrl", "redirectUrl", "some html data")))
        Mockito.`when`(extractorChain.extract(any(WebPageSource::class.java))).thenReturn(Flux.empty())
        val expectedEmptyResult = ContentWrapper(page.url, page.url, emptyList())
        val publisher = handler.extract(page)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedEmptyResult)
            .verifyComplete()
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}