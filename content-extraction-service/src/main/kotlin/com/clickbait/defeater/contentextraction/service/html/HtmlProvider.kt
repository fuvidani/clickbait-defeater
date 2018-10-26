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