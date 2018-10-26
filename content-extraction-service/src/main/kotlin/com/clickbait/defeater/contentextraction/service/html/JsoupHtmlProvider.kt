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
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Implementation of the [HtmlProvider] interface with the help of a powerful
 * HTML parser library called [Jsoup](https://jsoup.org/).
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupHtmlProvider : HtmlProvider {

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
    override fun get(webPage: WebPage): Mono<WebPageSource> {
        val url = tryAndGetRealUrl(webPage.url)
        @Suppress("DEPRECATION") // not validating TLS certificates may be problematic on Android, ignoring here
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0")
            .referrer("http://www.google.com")
            .followRedirects(true)
            .validateTLSCertificates(false)
            .get()
        return Mono.just(WebPageSource(webPage.url, document.location(), webPage.title, document.html()))
    }

    private fun tryAndGetRealUrl(url: String): String {
        var result = decodeUrl(appendProtocolIfNeeded(url))
        if (result.contains("https://l.facebook.com/l.php")) {
            val queryParams = UriComponentsBuilder.fromUriString(result).build().queryParams
            result = queryParams["u"]!![0]
        }
        return result
    }

    private fun appendProtocolIfNeeded(url: String): String {
        if (url.contains("http://") || url.contains("https://")) {
            return url
        }
        return "http://$url"
    }

    private fun decodeUrl(encodedUrl: String): String {
        return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.name())
    }
}