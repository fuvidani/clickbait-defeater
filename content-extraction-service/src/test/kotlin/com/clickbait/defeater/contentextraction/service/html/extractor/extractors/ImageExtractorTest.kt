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

import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.image.BoilerPipeImageExtractor
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
class ImageExtractorTest : AbstractExtractorTest(BoilerPipeImageExtractor()) {

    @Test
    fun `Given an html source, THEN extractor finds likely relevant images`() {
        val source = WebPageSource("redirectUrl", "title", realHtml)
        val publisher = extractor.extract(source, chain)
        // TODO image extractor must be enhanced, cannot test efficiently at the moment
        StepVerifier.create(publisher)
            .expectSubscription()
            .verifyComplete()
    }
}