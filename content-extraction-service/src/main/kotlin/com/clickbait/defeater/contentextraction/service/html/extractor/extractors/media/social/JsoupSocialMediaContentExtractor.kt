package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.social

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
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
@ExtractorBean(order = 5)
@Component
class JsoupSocialMediaContentExtractor(
    private val instagramExtractor: JsoupInstagramSocialMediaContentExtractor,
    private val twitterExtractor: JsoupTwitterSocialMediaContentExtractor,
    private val pinterestExtractor: JsoupPinterestSocialMediaContentExtractor
) : Extractor {

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        return Flux.concat(
            instagramExtractor.extract(document),
            twitterExtractor.extract(document),
            pinterestExtractor.extract(document),
            chain.extract(source)
        )
    }
}