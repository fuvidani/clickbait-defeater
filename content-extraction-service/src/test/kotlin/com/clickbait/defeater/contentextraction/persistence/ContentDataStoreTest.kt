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

package com.clickbait.defeater.contentextraction.persistence

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.persistence.cache.ContentCache
import com.clickbait.defeater.contentextraction.persistence.repository.ContentRepository
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
class ContentDataStoreTest {

    @MockBean
    private lateinit var repository: ContentRepository
    @MockBean
    private lateinit var cache: ContentCache
    private lateinit var dataStore: ContentDataStore

    @Before
    fun setUp() {
        Mockito.`when`(cache.tryAndGet("cachedUrl"))
            .thenReturn(Mono.just(TestData.getSampleContentWrapper("cachedUrl")))
        Mockito.`when`(cache.tryAndGet("notCachedUrl_inDb")).thenReturn(Mono.empty())
        Mockito.`when`(cache.tryAndGet("notCachedUrl_not_inDb")).thenReturn(Mono.empty())
        Mockito.`when`(cache.put(any(ContentWrapper::class.java))).thenReturn(Mono.just(true))
        Mockito.`when`(repository.findById("notCachedUrl_inDb"))
            .thenReturn(Mono.just(TestData.getSampleContentWrapper("notCachedUrl_inDb")))
        Mockito.`when`(repository.findById("notCachedUrl_not_inDb")).thenReturn(Mono.empty())
        Mockito.`when`(repository.save(any(ContentWrapper::class.java)))
            .thenReturn(Mono.just(TestData.getSampleContentWrapper("newUrl")))

        dataStore = DefaultContentDataStore(repository, cache)
    }

    @Test
    fun `Given an already cached URL, THEN cache returns corresponding content AND repository is not invoked`() {
        val publisher = dataStore.findById("cachedUrl")
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(TestData.getSampleContentWrapper("cachedUrl"))
            .verifyComplete()
        Mockito.verifyZeroInteractions(repository)
    }

    @Test
    fun `Given an URL not in cache but in DB, THEN cache returns empty and repository returns content`() {
        val publisher = dataStore.findById("notCachedUrl_inDb")
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(TestData.getSampleContentWrapper("notCachedUrl_inDb"))
            .verifyComplete()
        Mockito.verify(cache).tryAndGet("notCachedUrl_inDb")
        Mockito.verify(repository).findById("notCachedUrl_inDb")
    }

    @Test
    fun `Given an URL neither in cache nor in repository, THEN data store returns empty`() {
        val publisher = dataStore.findById("notCachedUrl_not_inDb")
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()
        Mockito.verify(cache).tryAndGet("notCachedUrl_not_inDb")
        Mockito.verify(repository).findById("notCachedUrl_not_inDb")
    }

    @Test
    fun `Given that client wants to store a new content, THEN content gets cached and persisted`() {
        val expectedContentWrapper = TestData.getSampleContentWrapper("newUrl")
        val publisher = dataStore.save(expectedContentWrapper)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedContentWrapper)
            .verifyComplete()
        Mockito.verify(cache).put(expectedContentWrapper)
        Mockito.verify(repository).save(expectedContentWrapper)
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}