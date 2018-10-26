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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.AbstractExtractorTest
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
class MetaDataExtractorTest : AbstractExtractorTest(JsoupMetaDataExtractor()) {

    @Test
    fun `Given an html source with metadata in it, THEN extractor returns it`() {
        val source = WebPageSource("redirectUrl", "redirectUrl", "title", testHtml)
        val publisher = extractor.extract(source, chain)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(MetaDataContent(MetaDataType.LANGUAGE, "en"))
            .expectNext(MetaDataContent(MetaDataType.KEYWORDS, "Some, keywords, helping, achieve, semantic, web"))
            .expectNext(MetaDataContent(MetaDataType.TITLE, "Test HTML file"))
            .expectNext(MetaDataContent(MetaDataType.DESCRIPTION, "This is an HTML file for testing various content extractors. It's content may be extended when new features, extractors get added."))
            .expectNext(MetaDataContent(MetaDataType.TIMESTAMP, "2018-08-15T10:45:11+01:00"))
            .expectNext(MetaDataContent(MetaDataType.IMAGE, "https://www.pexels.com/photo/laptop-calendar-and-books-908298/"))
            .expectNext(MetaDataContent(MetaDataType.VIDEO, "https://some-video.com/"))
            .verifyComplete()
    }

    @Test
    fun `Given an html source with no metadata, THEN extractor returns empty`() {
        val page = WebPageSource("someUrl", "someUrl", "someTitle", "")
        val publisher = extractor.extract(page, chain)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .verify()
    }
}