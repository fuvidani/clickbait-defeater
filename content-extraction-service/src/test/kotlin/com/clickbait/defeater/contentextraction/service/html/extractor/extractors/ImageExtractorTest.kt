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
class ImageExtractorTest : AbstractExtractorTest(BoilerPipeImageExtractor()) {

    @Test
    fun `Given an html source, THEN extractor finds likely relevant images`() {
        val source = WebPageSource("url","title", realHtml)
        val publisher = extractor.extract(source, chain)
        //TODO image extractor must be enhanced, cannot test efficiently at the moment
        StepVerifier.create(publisher)
            .expectSubscription()
            .verifyComplete()
    }
}