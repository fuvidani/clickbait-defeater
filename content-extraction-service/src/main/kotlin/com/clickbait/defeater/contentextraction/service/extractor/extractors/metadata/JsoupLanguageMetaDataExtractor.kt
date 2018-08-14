package com.clickbait.defeater.contentextraction.service.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MetaDataContent
import com.clickbait.defeater.contentextraction.model.MetaDataType
import org.jsoup.nodes.Document
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
internal class JsoupLanguageMetaDataExtractor {

    internal fun extract(document: Document): Flux<Content> {
        val lang = document.selectFirst("html").attr("lang")
        return if (lang.isNotEmpty()) {
            Flux.just(MetaDataContent(MetaDataType.LANGUAGE, lang))
        } else {
            Flux.empty()
        }
    }
}