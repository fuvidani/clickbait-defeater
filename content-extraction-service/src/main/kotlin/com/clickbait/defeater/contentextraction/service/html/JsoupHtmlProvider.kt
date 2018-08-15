package com.clickbait.defeater.contentextraction.service.html

import com.clickbait.defeater.contentextraction.model.WebPage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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

    override fun get(webPage: WebPage): Mono<String> {
        return Mono.just(
            @Suppress("DEPRECATION") // not validating TLS certificates may be problematic on Android, ignoring here
            Jsoup.connect(webPage.url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0")
                .referrer("http://www.google.com")
                .followRedirects(true)
                .validateTLSCertificates(false)
                .get()
                .html())
    }
}