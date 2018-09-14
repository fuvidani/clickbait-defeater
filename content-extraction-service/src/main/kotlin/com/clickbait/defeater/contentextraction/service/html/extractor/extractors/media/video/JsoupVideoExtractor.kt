package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
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

    private val domainsToIgnore = listOf("twitter.com/", "instagram.com/p/", "pinterest.com/")

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        return Flux
            .concat(
                naiveIFrameVideoExtractor.extract(document),
                brightCoveVideoExtractor.extract(document),
                youTubeVideoExtractor.extract(document),
                cnetVideoExtractor.extract(document),
                embedlyVideoExtractor.extract(document)
            )
            .filter { doesNotContainIgnoredDomain(it as MediaContent) }
            .concatWith(chain.extract(source))
    }

    private fun doesNotContainIgnoredDomain(content: MediaContent): Boolean {
        for (domain in domainsToIgnore) {
            if (content.src.contains(domain)) {
                return false
            }
        }
        return true
    }

    companion object : KLogging()
}