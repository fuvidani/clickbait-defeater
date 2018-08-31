package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import mu.KLogging
import org.jsoup.nodes.Document
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
@Component
class JsoupYouTubeVideoExtractor {

    internal fun extract(document: Document): Flux<Content> {
        return Flux.fromIterable(document.select("[src*=youtube.com/]"))
            .map { MediaContent(MediaType.VIDEO, it.attr("src")) }
            .doOnNext { JsoupBrightCoveVideoExtractor.logger.info("YouTube video found (${it.src})") }
            .map { it as Content }
    }

    companion object : KLogging()
}