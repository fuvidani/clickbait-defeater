package com.clickbait.defeater.contentextraction.service.extractor.extractors

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
import org.jsoup.Jsoup
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
class HtmlExtractor : Extractor {

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        @Suppress("DEPRECATION") // not validating TLS certificates may be problematic on Android, ignoring here
        val html = Jsoup.connect(source.url)
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0")
            .referrer("http://www.google.com")
            .followRedirects(true)
            .validateTLSCertificates(false)
            .get()
            .html()
        return chain.extract(WebPageSource(source.url, source.title, html))
    }
}