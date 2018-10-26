package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.social

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.SocialMediaContent
import com.clickbait.defeater.contentextraction.model.SocialMediaEmbeddingType
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Social media extractor specific to the [Twitter](https://twitter.com/) platform.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupTwitterSocialMediaContentExtractor {

    private val twitterEmbedUrlRegex = "https:\\/\\/twitter.com\\/[^\\/]*\\/status\\/.*"

    /**
     * Extracts multiple potential embedded "tweets" as social media
     * content from the given `document`.
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found social media [Content]s
     */
    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("a[href~=$twitterEmbedUrlRegex]"))
            .map { SocialMediaContent(SocialMediaEmbeddingType.TWITTER, it.attr("href")) }
    }
}