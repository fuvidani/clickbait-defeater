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