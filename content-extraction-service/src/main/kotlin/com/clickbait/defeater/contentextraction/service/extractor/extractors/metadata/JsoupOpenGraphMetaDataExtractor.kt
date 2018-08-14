package com.clickbait.defeater.contentextraction.service.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MetaDataContent
import com.clickbait.defeater.contentextraction.model.MetaDataType
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JsoupOpenGraphMetaDataExtractor {

    internal fun extract(document: Document): Flux<Content> {
        return document
            .getElementsByTag("meta")
            .filter { isOgElement(it) }
            .mapNotNull { ogElementToContent(it) }
            .toFlux()
    }

    private fun isOgElement(element: Element): Boolean {
        val property = element.attr("property")
        val content = element.attr("content")
        return property.isNotBlank() &&
                content.isNotBlank() &&
                property.startsWith("og:")
    }

    private fun ogElementToContent(element: Element): Content? {
        val ogContent = element.attr("content")
        val ogProperty = element.attr("property")
        return when (ogProperty) {
            "og:title" -> MetaDataContent(MetaDataType.TITLE, ogContent)
            "og:description" -> MetaDataContent(MetaDataType.DESCRIPTION, ogContent)
            "og:image" -> MetaDataContent(MetaDataType.IMAGE, ogContent)
            "og:video" -> MetaDataContent(MetaDataType.VIDEO, ogContent)
            "og:updated_time" -> MetaDataContent(MetaDataType.TIMESTAMP, ogContent)
            // add newly supported og tag<->content mapping here
            else -> null
        }
    }
}