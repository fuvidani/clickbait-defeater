package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * A [Jsoup](https://jsoup.org/)-based meta-data [Extractor] implementation responsible
 * for extracting various types of meta-data of a web page/article.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtractorBean(order = 3)
@Component
class JsoupMetaDataExtractor : Extractor {

    private val languageMetaDataExtractor = JsoupLanguageMetaDataExtractor()
    private val keywordsMetaDataExtractor = JsoupKeywordsMetaDataExtractor()
    private val openGraphMetaDataExtractor = JsoupOpenGraphMetaDataExtractor()

    /**
     * Performs the extraction process on the given `source` and
     * (optionally) delegates to the next [Extractor] through
     * the given [ExtractorChain]. The result of this extractor
     * and of the chain are published through a single [Flux].
     *
     * @param source the source of a web page from which the
     * contents should be extracted
     * @param chain the chain to allow delegation to the next
     * [Extractor]
     * @return a Flux of [Content] extracted by this extractor
     * and optionally of other [Extractor]s in the chain (in
     * case of a delegation)
     */
    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val document = Jsoup.parse(source.html)
        return Flux.concat(
            languageMetaDataExtractor.extract(document),
            keywordsMetaDataExtractor.extract(document),
            openGraphMetaDataExtractor.extract(document),
            chain.extract(source)
        )
    }
}