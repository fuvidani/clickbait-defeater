package com.clickbait.defeater.contentextraction.service.html.extractor.extractors

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.jsoup.Jsoup
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
class JsoupVideoExtractor : Extractor {

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        val elements = document.getElementsByTag("iframe")
        val videos: List<Content> = elements.filter { element ->
            var height = element.attr("height")
            var width = element.attr("width")
            if (height.isNotEmpty() && width.isNotEmpty()) {
                if (height.endsWith("%")) {
                    height = height.dropLast(1)
                }
                if (height.endsWith("px")) {
                    height = height.dropLast(2)
                }
                if (width.endsWith("%")) {
                    width = width.dropLast(1)
                }
                if (width.endsWith("px")) {
                    width = width.dropLast(2)
                }
                height.toInt() > 0 && width.toInt() > 0
            } else {
                false
            }
        }.map { MediaContent(MediaType.VIDEO, it.attr("src")) }
        val videoFlux = Flux.fromIterable(videos)
        return Flux.concat(videoFlux, chain.extract(source))
    }
}