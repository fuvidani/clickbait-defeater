package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.persistence.ContentDataStore
import com.clickbait.defeater.contentextraction.persistence.TestData
import com.clickbait.defeater.contentextraction.service.handler.ContentExtractionHandler
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
class ContentExtractionServiceTest {

    private lateinit var service: ContentExtractionService
    @MockBean
    private lateinit var dataStore: ContentDataStore
    @MockBean
    private lateinit var handler: ContentExtractionHandler

    @Before
    fun setUp() {
        service = DefaultContentExtractionService(handler, dataStore)
    }

    @Test
    fun `Given a valid WebPage AND already extracted content, THEN service returns content AND handler is not invoked`() {
        val webPage = WebPage("redirectUrl", "title")
        val expectedContent = TestData.getSampleContentWrapper("redirectUrl")
        Mockito.`when`(dataStore.findById("redirectUrl")).thenReturn(Mono.just(expectedContent))

        val publisher = service.extractContent(webPage)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedContent)
            .verifyComplete()
        Mockito.verifyZeroInteractions(handler)
    }

    @Test
    fun `Given a valid WebPage not cached or persisted, THEN service returns extracted content`() {
        val webPage = WebPage("redirectUrl", "title")
        val expectedContent = TestData.getSampleContentWrapper("redirectUrl")
        Mockito.`when`(dataStore.findById("redirectUrl")).thenReturn(Mono.empty())
        Mockito.`when`(dataStore.save(expectedContent)).thenReturn(Mono.just(expectedContent))
        Mockito.`when`(handler.extract(webPage)).thenReturn(Flux.fromIterable(expectedContent.contents))

        val publisher = service.extractContent(webPage)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedContent)
            .verifyComplete()
    }
}