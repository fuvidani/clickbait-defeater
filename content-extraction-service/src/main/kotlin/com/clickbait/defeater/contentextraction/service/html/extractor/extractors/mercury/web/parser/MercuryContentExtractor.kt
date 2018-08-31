package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.mercury.web.parser

import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.model.MercuryApiResponse
import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.MediaContent
import com.clickbait.defeater.contentextraction.model.HtmlContent
import com.clickbait.defeater.contentextraction.model.MediaType
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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
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
            document.select(blackListCssQuery).forEach { it.remove() }
            document.outputSettings(htmlOutputSettings)
            val html = document.body().html()
                .replace("'", "&apos;")
                .replace("\n", "")
            Flux.concat(extractArticleImages(document), Flux.just(HtmlContent(html)))
        } else {
            Flux.empty()
        }
    }

    private fun extractArticleImages(document: Document): Flux<Content> {
        return Flux
            .fromIterable(document.select("img[src~=^((?:(?!http).)*http(?!.*http))?(?!http).*\$]"))
            .map { MediaContent(MediaType.IMAGE, it.attr("src")) }
    }

    companion object : KLogging()
}