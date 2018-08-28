package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.social

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.SocialMediaContent
import com.clickbait.defeater.contentextraction.model.SocialMediaEmbeddingType
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
class JsoupPinterestSocialMediaContentExtractor {

    private val pinterestUrlRegex = "https:\\/\\/www.pinterest.com\\/.+"

    internal fun extract(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("a[href~=$pinterestUrlRegex]"))
            .map { SocialMediaContent(SocialMediaEmbeddingType.PINTEREST, it.attr("href")) }
    }
}