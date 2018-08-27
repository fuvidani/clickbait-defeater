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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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

    private val blackListCssQuery = "[class*=social], figure, [class*=image], [class*=img]"

    override fun extract(source: WebPageSource, chain: ExtractorChain): Flux<Content> {
        val content = apiClient.getArticleContent(apiKey, source.url)
            .flatMapMany {
                mapResponse(it)
            }
        return Flux.concat(content, chain.extract(source))
    }

    private fun mapResponse(response: MercuryApiResponse): Flux<Content> {
        val contents: MutableList<Content> = mutableListOf()
        if (response.lead_image_url.isNotBlank()) {
            contents.add(MediaContent(MediaType.IMAGE, response.lead_image_url))
        }
        if (response.content.isNotBlank()) {
            val html = response.content
            val document = Jsoup.parse(html)
            removeElementsMatching(blackListCssQuery, document)
            document.outputSettings(Document.OutputSettings().prettyPrint(false))
            contents.add(HtmlContent(document.body().html()))
        }
        return Flux.fromIterable(contents)
    }

    private fun removeElementsMatching(cssQuery: String, document: Document) {
        document.select(cssQuery).forEach { it.remove() }
    }
}