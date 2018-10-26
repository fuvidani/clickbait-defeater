package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MetaDataContent
import com.clickbait.defeater.contentextraction.model.MetaDataType
import org.jsoup.nodes.Document
import reactor.core.publisher.Flux

/**
 * Meta-data extractor specialized for extracting keywords from a Jsoup [Document].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JsoupKeywordsMetaDataExtractor {

    /**
     * Extracts keywords from the meta-data part of the provided
     * `document` and emits them in a [Flux].
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the extracted keywords
     */
    internal fun extract(document: Document): Flux<Content> {
        val keywords = document.getElementsByTag("meta")
            .filter {
                it.attr("name").isNotBlank() &&
                        it.attr("name").contains("keywords") &&
                        it.attr("content").isNotBlank()
            }.map { MetaDataContent(MetaDataType.KEYWORDS, it.attr("content")) }
            .firstOrNull()
        return if (keywords != null) {
            Flux.just(keywords)
        } else {
            Flux.empty()
        }
    }
}