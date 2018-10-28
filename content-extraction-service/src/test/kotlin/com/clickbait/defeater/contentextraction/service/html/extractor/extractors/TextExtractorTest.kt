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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors

import com.clickbait.defeater.contentextraction.model.TextContent
import com.clickbait.defeater.contentextraction.model.WebPageSource
import org.junit.Test
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
class TextExtractorTest : AbstractExtractorTest(BoilerPipeTextExtractor()) {

    @Test
    fun `Given an html source, THEN extractor returns relevant text content`() {
        val page = WebPageSource("someUrl", "someUrl", "someTitle", testHtml)
        val publisher = extractor.extract(page, chain)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(TextContent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."))
            .expectNext(TextContent("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua."))
            .expectNext(TextContent("At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."))
            .verifyComplete()
    }

    @Test
    fun `Given an html source with no text content, THEN extractor returns empty`() {
        val page = WebPageSource("someUrl", "someTitle", "")
        val publisher = extractor.extract(page, chain)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .verify()
    }
}