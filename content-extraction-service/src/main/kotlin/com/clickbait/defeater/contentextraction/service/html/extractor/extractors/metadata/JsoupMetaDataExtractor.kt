package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.jsoup.Jsoup
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
class JsoupMetaDataExtractor : Extractor {

    private val languageMetaDataExtractor = JsoupLanguageMetaDataExtractor()
    private val keywordsMetaDataExtractor = JsoupKeywordsMetaDataExtractor()
    private val openGraphMetaDataExtractor = JsoupOpenGraphMetaDataExtractor()

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        return Flux.concat(
            languageMetaDataExtractor.extract(document),
            keywordsMetaDataExtractor.extract(document),
            openGraphMetaDataExtractor.extract(document),
            chain.extract(source))
    }
}