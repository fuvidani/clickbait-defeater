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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupCnetVideoExtractor {

    private val jsonParser = JsonParser()

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