package com.clickbait.defeater.contentextraction.service.html.extractor.extractors

import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
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
class VideoExtractorTest : AbstractExtractorTest(JsoupVideoExtractor()) {

    @Test
    fun `Given an html source with an important video with it, THEN extractor returns it`() {
        val source = WebPageSource("url", "title", testHtml)
        val publisher = extractor.extract(source, chain)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com"))
            .verifyComplete()
    }
}