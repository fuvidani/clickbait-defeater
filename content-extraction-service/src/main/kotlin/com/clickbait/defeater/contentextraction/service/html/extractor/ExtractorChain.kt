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
 * Contract to allow an [Extractor] to delegate to the next one
 * in the chain. This contract/structure follows the pattern
 * described in [org.springframework.web.server.WebFilterChain].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ExtractorChain {

    /**
     * Delegate to the next [Extractor] in the chain.
     *
     * @param source the web page source each [Extractor]
     * should process
     * @return a Flux of [Content] published by the chain;
     * the end of the chain emits a complete signal thus
     * ensuring a finite stream
     */
    fun extract(source: WebPageSource): Flux<Content>
}