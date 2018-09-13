package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.video

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import mu.KLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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
class JsoupNaiveIFrameVideoExtractor {

    // matches 10, 100, 100px, 100%, ...
    // doesn't match 0, 1, 2
    private val regex = "\\d{2,}(px|%)*"
    private val sourceChunks = listOf("youtube.com/", "brightcove", "plugins/like.php?", "/recaptcha",".php")

    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("iframe[width~=$regex],  iframe[height~=$regex]"))
            .filter { shouldKeep(it) }
            .map { MediaContent(MediaType.VIDEO, it.attr("src")) }
            .doOnNext { logger.info("IFrame video found (${it.src})") }
            .map { it as Content }
    }

    private fun shouldKeep(element: Element): Boolean {
        val src = element.attr("src")
        return src.isNotEmpty() && isNotComposedOfChunks(src)
    }

    private fun isNotComposedOfChunks(source: String): Boolean {
        for (chunk in sourceChunks) {
            if (source.contains(chunk)) {
                return false
            }
        }
        return true
    }

    companion object : KLogging()
}