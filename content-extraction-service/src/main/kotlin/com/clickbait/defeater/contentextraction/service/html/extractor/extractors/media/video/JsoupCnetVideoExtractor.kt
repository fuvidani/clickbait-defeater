package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import com.google.gson.JsonParser
import mu.KLogging
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Video media extractor specific to the [Cnet](https://www.cnet.com/) platform.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupCnetVideoExtractor {

    private val jsonParser = JsonParser()

    /**
     * Extracts multiple potential video content from
     * the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found video [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("[data-video-playlist]"))
            .flatMap { extractUrlFromJson(it.attr("data-video-playlist")) }
            .map { MediaContent(MediaType.VIDEO, it) }
            .doOnNext { logger.info("Cnet video found (${it.src})") }
            .map { it as Content }
    }

    private fun extractUrlFromJson(content: String): Mono<String> {
        val array = jsonParser.parse(content).asJsonArray
        val url = array[0].asJsonObject.get("mp4").asString
        return Mono.just(url)
    }

    companion object : KLogging()
}