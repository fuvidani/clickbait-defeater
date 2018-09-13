package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import mu.KLogging
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtractorBean(order = 4)
@Component
class JsoupVideoExtractor(
    private val naiveIFrameVideoExtractor: JsoupNaiveIFrameVideoExtractor,
    private val brightCoveVideoExtractor: JsoupBrightCoveVideoExtractor,
    private val youTubeVideoExtractor: JsoupYouTubeVideoExtractor,
    private val cnetVideoExtractor: JsoupCnetVideoExtractor,
    private val embedlyVideoExtractor: JsoupEmbedlyVideoExtractor
) : Extractor {

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        return Flux.concat(
            naiveIFrameVideoExtractor.extract(document),
            brightCoveVideoExtractor.extract(document),
            youTubeVideoExtractor.extract(document),
            cnetVideoExtractor.extract(document),
            embedlyVideoExtractor.extract(document),
            chain.extract(source))
    }

    companion object : KLogging()
}