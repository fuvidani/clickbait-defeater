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