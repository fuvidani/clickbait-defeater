package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.social

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux

/**
 * Social media extractor specific to the [Instagram](https://www.instagram.com/) platform.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupInstagramSocialMediaContentExtractor {

    /**
     * Extracts multiple potential Instagram posts as social media
     * content from the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found social media [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("a[href*=instagram.com/p/]"))
            .map { SocialMediaContent(SocialMediaEmbeddingType.INSTAGRAM, getTrimmedInstagramUrl(it.attr("href"))) }
    }

    private fun getTrimmedInstagramUrl(source: String): String {
        val components = UriComponentsBuilder.fromUriString(source).build()
        return "https://www.instagram.com${components.path}"
    }
}