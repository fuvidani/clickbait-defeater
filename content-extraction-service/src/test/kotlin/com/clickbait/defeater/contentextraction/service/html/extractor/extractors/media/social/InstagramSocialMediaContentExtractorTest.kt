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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.social

import com.clickbait.defeater.contentextraction.model.SocialMediaContent
import com.clickbait.defeater.contentextraction.model.SocialMediaEmbeddingType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
class InstagramSocialMediaContentExtractorTest {

    private val extractor = JsoupInstagramSocialMediaContentExtractor()

    @Test
    fun `GIVEN an article with an embedded instagram post in it, THEN extractor extracts link AND cuts away parameters`() {
        val url = "https://www.instagram.com/p/BnoSjf6h2Qm/?taken-by=timeavajna"
        val document = getDocumentWithTestUrl(url)
        StepVerifier.create(extractor.extract(document))
            .expectSubscription()
            .expectNext(SocialMediaContent(SocialMediaEmbeddingType.INSTAGRAM, "https://www.instagram.com/p/BnoSjf6h2Qm/"))
            .expectComplete()
            .log()
            .verify()
    }

    private fun getDocumentWithTestUrl(originalUrl: String): Document {
        val html = String.format(testHtml, originalUrl)
        return Jsoup.parse(html)
    }

    private val testHtml = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Title</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<a href=\"%s\"></a>\n" +
            "</body>\n" +
            "</html>"
}