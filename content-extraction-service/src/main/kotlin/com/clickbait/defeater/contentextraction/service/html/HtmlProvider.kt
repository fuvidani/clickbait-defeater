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

package com.clickbait.defeater.contentextraction.service.html

import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.model.WebPageSource
import reactor.core.publisher.Mono

/**
 * Interface for a general HTML provider component.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface HtmlProvider {

    /**
     * Tries to get the HTML source code of the given
     * `webPage`. The result is returned in a [Mono]
     * inside a [WebPageSource] object.
     *
     * @param webPage a valid [WebPage] object describing
     * the web page for which the HTML source code should
     * be retrieved
     * @return the source of the requested web page emitted
     * by a [Mono]
     */
    fun get(webPage: WebPage): Mono<WebPageSource>
}