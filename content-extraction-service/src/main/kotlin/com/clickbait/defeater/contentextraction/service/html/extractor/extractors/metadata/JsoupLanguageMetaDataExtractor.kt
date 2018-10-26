package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MetaDataContent
import com.clickbait.defeater.contentextraction.model.MetaDataType
import org.jsoup.nodes.Document
import reactor.core.publisher.Flux

/**
 * Meta-data extractor specialized for extracting the language identifier
 * from a Jsoup [Document].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JsoupLanguageMetaDataExtractor {

    /**
     * Extracts the language identifier from the given `document`
     * and emits it in a [Flux].
     *
     * @param document a valid HTML document of [org.jsoup.Jsoup]
     * @return a Flux emitting the found language
     */
    internal fun extract(document: Document): Flux<Content> {
        val lang = document.selectFirst("html").attr("lang")
        return if (lang.isNotEmpty()) {
            Flux.just(MetaDataContent(MetaDataType.LANGUAGE, lang))
        } else {
            Flux.empty()
        }
    }
}