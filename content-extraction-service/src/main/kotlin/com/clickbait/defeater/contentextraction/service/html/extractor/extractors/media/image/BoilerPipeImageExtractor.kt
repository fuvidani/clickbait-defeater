/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.media.image

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.MediaType
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import com.kohlschutter.boilerpipe.BoilerpipeExtractor
import com.kohlschutter.boilerpipe.extractors.CommonExtractors
import com.kohlschutter.boilerpipe.sax.BoilerpipeSAXInput
import com.kohlschutter.boilerpipe.sax.HTMLDocument
import com.kohlschutter.boilerpipe.sax.ImageExtractor
import mu.KLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * An image [Extractor] implementation based on the [BoilerPipe](https://boilerpipe-web.appspot.com/)
 * library optimized for web articles.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtractorBean(order = 2)
@Component
class BoilerPipeImageExtractor : Extractor {

    private val imageExtractor = ImageExtractor.INSTANCE

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
        val articleExtractorImages = extractImagesWitExtractor(source, CommonExtractors.ARTICLE_EXTRACTOR)
        logger.info("${javaClass.simpleName} found ${articleExtractorImages.size} images")
        return Flux.concat(Flux.fromIterable(articleExtractorImages), chain.extract(source))
    }

    private fun extractImagesWitExtractor(source: WebPageSource, extractor: BoilerpipeExtractor): List<Content> {
        val textDocument = BoilerpipeSAXInput(HTMLDocument(source.html).toInputSource()).textDocument
        extractor.process(textDocument)
        return imageExtractor.process(textDocument, source.html)
            .filter { it.width != null && it.height != null }
            .map { MediaContent(MediaType.IMAGE, it.src) }
    }

    companion object : KLogging()
}