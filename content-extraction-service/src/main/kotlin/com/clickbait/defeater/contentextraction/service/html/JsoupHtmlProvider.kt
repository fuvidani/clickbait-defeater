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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class JsoupHtmlProvider : HtmlProvider {

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