package com.clickbait.defeater.contentextraction.service.extractor.extractors

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.TextContent
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
import com.kohlschutter.boilerpipe.extractors.CommonExtractors
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
class BoilerPipeTextExtractor : Extractor {

    private val articleExtractor = CommonExtractors.ARTICLE_EXTRACTOR

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val textBlocks: List<Content> = articleExtractor.getText(source.html)
            .split("\n")
            .filter { it.isNotBlank() }
            .filter { it.split(" ").size > 5 }
            .map { TextContent(it) }
        return Flux.concat(Flux.fromIterable(textBlocks), chain.extract(source))
    }
}