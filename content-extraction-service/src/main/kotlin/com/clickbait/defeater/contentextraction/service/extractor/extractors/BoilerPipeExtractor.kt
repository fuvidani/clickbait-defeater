package com.clickbait.defeater.contentextraction.service.extractor.extractors

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import com.clickbait.defeater.contentextraction.service.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
import com.kohlschutter.boilerpipe.extractors.CommonExtractors
import com.kohlschutter.boilerpipe.sax.BoilerpipeSAXInput
import com.kohlschutter.boilerpipe.sax.HTMLDocument
import com.kohlschutter.boilerpipe.sax.ImageExtractor
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
class BoilerPipeExtractor : Extractor {

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val extractor = CommonExtractors.ARTICLE_EXTRACTOR
        val textBlocks: List<Content> = extractor.getText(source.html)
            .split("\n")
            .filter { it.isNotEmpty() }
            .map { TextContent(it) }
        val imageExtractor = ImageExtractor.INSTANCE
        val textDocument = BoilerpipeSAXInput(HTMLDocument(source.html).toInputSource()).textDocument
        val images: List<Content> =
            imageExtractor.process(textDocument, source.html).map { MediaContent(MediaType.IMAGE, it.src) }

        CommonExtractors.DEFAULT_EXTRACTOR.process(textDocument)
        val images2: List<Content> =
            imageExtractor.process(textDocument, source.html).map { MediaContent(MediaType.IMAGE, it.src) }

        val textFlux = Flux.fromIterable(textBlocks)
        val imageFlux = Flux.fromIterable(images)

        return Flux.concat(textFlux, imageFlux, Flux.fromIterable(images2), chain.extract(source))
    }
}