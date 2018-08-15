package com.clickbait.defeater.contentextraction.service.html.extractor.extractors

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import com.kohlschutter.boilerpipe.BoilerpipeExtractor
import com.kohlschutter.boilerpipe.extractors.CommonExtractors
import com.kohlschutter.boilerpipe.sax.BoilerpipeSAXInput
import com.kohlschutter.boilerpipe.sax.HTMLDocument
import com.kohlschutter.boilerpipe.sax.ImageExtractor
import reactor.core.publisher.Flux
import java.util.logging.Logger

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class BoilerPipeImageExtractor : Extractor {

    private val log: Logger = Logger.getLogger(this.javaClass.name)
    private val imageExtractor = ImageExtractor.INSTANCE

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val articleExtractorImages = extractImagesWitExtractor(source, CommonExtractors.ARTICLE_EXTRACTOR)
        log.info("${javaClass.simpleName} found ${articleExtractorImages.size} images")
        return Flux.concat(Flux.fromIterable(articleExtractorImages), chain.extract(source))
    }

    private fun extractImagesWitExtractor(source: WebPageSource, extractor: BoilerpipeExtractor): List<Content> {
        val textDocument = BoilerpipeSAXInput(HTMLDocument(source.html).toInputSource()).textDocument
        extractor.process(textDocument)
        return imageExtractor.process(textDocument, source.html)
            .filter { it.width != null && it.height != null }
            .map { MediaContent(MediaType.IMAGE, it.src) }
    }
}