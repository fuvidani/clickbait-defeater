package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import mu.KLogging
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Video media extractor specific to the [Embedly](https://embed.ly/) platform.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupEmbedlyVideoExtractor {

    /**
     * Extracts multiple potential video content from
     * the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found video [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux.fromIterable(document.select("a[class*=embedly]"))
            .map { MediaContent(MediaType.VIDEO, it.attr("href")) }
            .doOnNext { logger.info("Embedly media found (${it.src})") }
            .map { it as Content }
    }

    companion object : KLogging()
}