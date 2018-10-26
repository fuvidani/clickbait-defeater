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

package com.clickbait.defeater.clickbaitservice.read.service.language.detector

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
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
class LanguageDetectorTest {

    private lateinit var detector: LanguageDetector
    private val libraryDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
        .withProfiles(LanguageProfileReader().readAllBuiltIn())
        .build()

    @Before
    fun setUp() {
        detector = PostLanguageDetector(libraryDetector)
    }

    @Test
    fun `test detect with ENGLISH post, should detect correct language`() {
        val post = PostInstance("id", postText = listOf("See the $1.5 million Kickstarter - only 1 day left"))
        val publisher = detector.detect(post)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(PostInstance(post.id, "en", post.postText))
            .expectComplete()
            .verify()
    }

    @Test
    fun `test detect with GERMAN post, should detect correct language`() {
        val post = PostInstance(
            "id",
            postText = listOf("21 Beweise, dass \"Frauentausch\" die allerbeste Sendung der Welt ist")
        )
        val publisher = detector.detect(post)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(PostInstance(post.id, "de", post.postText))
            .expectComplete()
            .verify()
    }

    @Test
    fun `test detect with SPANISH post, should detect correct language`() {
        val post = PostInstance("id", postText = listOf("los negocios para los que no estaba capacitado"))
        val publisher = detector.detect(post)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(PostInstance(post.id, "es", post.postText))
            .expectComplete()
            .verify()
    }

    @Test
    fun `test detect with HUNGARIAN post, should detect correct language`() {
        val post =
            PostInstance("id", postText = listOf("Az oroszok abszurd színháznak nevezték az amerikai szankciókat"))
        val publisher = detector.detect(post)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(PostInstance(post.id, "hu", post.postText))
            .expectComplete()
            .verify()
    }

    @Test
    fun `test detect with unrecognizable language post, should determine language as unknown`() {
        val post = PostInstance("id", postText = listOf(""))
        val publisher = detector.detect(post)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(PostInstance(post.id, "unknown", post.postText))
            .expectComplete()
            .verify()
    }
}