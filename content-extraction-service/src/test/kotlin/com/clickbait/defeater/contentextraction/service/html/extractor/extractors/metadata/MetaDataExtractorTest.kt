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