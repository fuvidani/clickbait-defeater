package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import mu.KLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Video media extractor specific to the [BrightCove](https://www.brightcove.com/en/) platform.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupBrightCoveVideoExtractor {

    private val brightCoveVideoUrlTemplate = "https://players.brightcove.net/%s/%s_default/index.html?videoId=%s"

    /**
     * Extracts multiple potential video content from
     * the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found video [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux.concat(
            extractBrightCoveIFrameVideoUrl(document),
            extractAmpBrightCoveVideoUrl(document)
        )
    }

    private fun extractBrightCoveIFrameVideoUrl(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("iframe[class*=brightcove]"))
            .filter { it.attr("src").isNotBlank() }
            .map { MediaContent(MediaType.VIDEO, it.attr("src")) }
            .doOnNext { logger.info("BrightCove video found (${it.src})") }
            .map { it as Content }
    }

    private fun extractAmpBrightCoveVideoUrl(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.getElementsByTag("amp-brightcove"))
            .flatMap { urlFromBrightCoveAttributes(it) }
            .map { MediaContent(MediaType.VIDEO, it) }
            .doOnNext { logger.info("BrightCove video found (${it.src})") }
            .map { it as Content }
    }

    private fun urlFromBrightCoveAttributes(htmlElement: Element): Mono<String> {
        val account = htmlElement.attr("data-account")
        val dataPlayer = htmlElement.attr("data-player")
        val videoId = htmlElement.attr("data-video-id")
        val url = String.format(brightCoveVideoUrlTemplate, account, dataPlayer, videoId)
        return Mono.just(url)
    }

    companion object : KLogging()
}