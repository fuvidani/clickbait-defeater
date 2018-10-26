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

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video.*
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
class VideoExtractorTest : AbstractExtractorTest(JsoupVideoExtractor(
    JsoupNaiveIFrameVideoExtractor(), JsoupBrightCoveVideoExtractor(),
    JsoupYouTubeVideoExtractor(), JsoupCnetVideoExtractor(), JsoupEmbedlyVideoExtractor()
)) {

    @Test
    fun `Given an html source with an important video with it, THEN extractor returns it`() {
        val source = WebPageSource("redirectUrl", "redirectUrl", "title", testHtml)
        val publisher = extractor.extract(source, chain)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com"))
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com/2"))
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com/3"))
            .verifyComplete()
    }
}