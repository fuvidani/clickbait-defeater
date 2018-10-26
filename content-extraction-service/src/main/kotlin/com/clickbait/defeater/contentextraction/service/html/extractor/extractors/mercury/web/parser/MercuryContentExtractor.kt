package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.mercury.web.parser

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.mercury.web.parser.client.MercuryWebParserApiClient
import mu.KLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * A composite [Extractor] implementation leveraging the
 * [Mercury Web Parser API](https://mercury.postlight.com/web-parser/). While the accuracy of this
 * extractor mainly depends on the result of the API, it supports the extraction of article images
 * and relevant text content both in String and HTML representation.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property apiClient a [MercuryWebParserApiClient] implementation
 * @property apiKey a valid API-key for the Web-parser
 * @property blackListCssQuery a CSS query containing elements which should not be part
 * of the textual content
 * @property htmlOutputSettings specific output settings when a [Document] is converted
 * into a String representation
 */
@ExtractorBean
@Component
class MercuryContentExtractor(
    private val apiClient: MercuryWebParserApiClient,
    @Value("\${mercury.web.parser.api.key}")
    private val apiKey: String
) : Extractor {

    private val blackListCssQuery = "[class*=social], figure, [class*=image], [class*=img], iframe[src~=.{10,}]"
    private val htmlOutputSettings = Document.OutputSettings()
        .escapeMode(Entities.EscapeMode.extended)
        .charset("ASCII")
        .prettyPrint(false)

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
        val content = apiClient
            .getArticleContent(apiKey, source.sourceUrl)
            .flatMapMany { mapResponse(it) }
            .onErrorResume {
                logger.warn("MercuryWebParser API returned error for url ${source.sourceUrl}. Error: $it")
                Flux.empty()
            }
        return Flux.concat(content, chain.extract(source))
    }

    private fun mapResponse(response: MercuryApiResponse): Flux<Content> {
        return Flux.concat(extractLeadImage(response), extractHtmlContent(response))
    }

    private fun extractLeadImage(response: MercuryApiResponse): Flux<Content> {
        return if (response.lead_image_url != null && response.lead_image_url.isNotBlank()) {
            Flux.just(MediaContent(MediaType.IMAGE, response.lead_image_url))
        } else {
            Flux.empty()
        }
    }

    private fun extractHtmlContent(response: MercuryApiResponse): Flux<Content> {
        return if (response.content != null && response.content.isNotBlank()) {
            val document = Jsoup.parse(response.content)
            val images = extractArticleImages(document)
            document.select(blackListCssQuery).forEach { it.remove() }
            document.outputSettings(htmlOutputSettings)
            val html = document.body().html()
                .replace("'", "&apos;")
                .replace("\n", "")
            Flux.concat(
                images,
                Flux.just(TextContent(document.body().text())),
                Flux.just(HtmlContent(html))
            )
        } else {
            Flux.empty()
        }
    }

    private fun extractArticleImages(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("img[src~=^((?:(?!http).)*http(?!.*http))?(?!http).*\$], amp-img[src~=^((?:(?!http).)*http(?!.*http))?(?!http).*\$]"))
            .filter { filterImageSrc(it.attr("src")) }
            .map { MediaContent(MediaType.IMAGE, it.attr("src")) }
            .map { it as Content }
            .concatWith(extractFigureImages(document))
    }

    private fun filterImageSrc(source: String): Boolean {
        if (source.contains(".svg")) {
            if (source.contains("facebook") || source.contains("instagram") || source.contains("youtube")) {
                return false
            }
        }
        return true
    }

    private fun extractFigureImages(document: Document): Flux<Content> {
        return Flux.fromIterable(document.select("figure a[href~=^((?:(?!http).)*http(?!.*http))?(?!http).*\$]"))
            .map { MediaContent(MediaType.IMAGE, it.attr("href")) }
    }

    companion object : KLogging()
}