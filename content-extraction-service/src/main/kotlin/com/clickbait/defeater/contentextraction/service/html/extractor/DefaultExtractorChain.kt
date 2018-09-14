package com.clickbait.defeater.contentextraction.service.html.extractor

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
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
class DefaultExtractorChain(
    private val extractors: List<Extractor>,
    private val index: Int = 0
) :
    ExtractorChain {

    private constructor(parent: DefaultExtractorChain, index: Int) : this(parent.extractors, index)

    override fun extract(source: WebPageSource): Flux<Content> {
        return Flux.defer {
            if (index < extractors.size) {
                val extractor = extractors[index]
                val chain =
                    DefaultExtractorChain(this, index + 1)
                extractor.extract(source, chain)
            } else {
                Flux.empty()
            }
        }
    }
}