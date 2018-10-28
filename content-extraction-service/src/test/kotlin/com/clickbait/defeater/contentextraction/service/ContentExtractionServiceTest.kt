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
        Mockito.`when`(handler.extract(webPage)).thenReturn(Mono.just(expectedContent))

        val publisher = service.extractContent(webPage)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedContent)
            .verifyComplete()
    }
}