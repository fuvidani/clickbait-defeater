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

package com.clickbait.defeater.contentextraction.service.html.extractor

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import reactor.core.publisher.Flux

/**
 * Default implementation of [ExtractorChain] interface which uses
 * an outside-controlled list of [Extractor] objects in the chain.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property extractors an ordered list of [Extractor] implementations
 * which the chain should go through
 * @property index the current index indicating the progress in the chain;
 * defaults to 0
 */
class DefaultExtractorChain(
    private val extractors: List<Extractor>,
    private val index: Int = 0
) :
    ExtractorChain {

    /**
     * Constructs a new instance with the same extractors of the parent and the
     * provided `index`.
     *
     * @param parent the parent chain object
     * @param index the new position in the chain
     */
    private constructor(parent: DefaultExtractorChain, index: Int) : this(parent.extractors, index)

    /**
     * Delegate to the next [Extractor] in the chain.
     *
     * @param source the web page source each [Extractor]
     * should process
     * @return a Flux of [Content] published by the chain;
     * the end of the chain emits a complete signal thus
     * ensuring a finite stream
     */
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