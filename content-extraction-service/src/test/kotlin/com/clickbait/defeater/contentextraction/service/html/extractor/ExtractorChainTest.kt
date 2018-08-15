package com.clickbait.defeater.contentextraction.service.html.extractor

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.BoilerPipeImageExtractor
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.BoilerPipeTextExtractor
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.JsoupVideoExtractor
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata.JsoupMetaDataExtractor
import org.jsoup.Jsoup
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import reactor.test.StepVerifier
import java.io.File

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringRunner::class)
class ExtractorChainTest {

    private lateinit var chain: ExtractorChain
    private val testHtml: String = loadHtmlFromResources("test_html.html")

    @Before
    fun setUp() {
        chain = DefaultExtractorChain(listOf(
            BoilerPipeTextExtractor(),
            BoilerPipeImageExtractor(),
            JsoupMetaDataExtractor(),
            JsoupVideoExtractor()
        ))
    }

    @Test
    fun `Given a valid input AND an extractor chain, THEN chain gets traversed in order`() {
        val source = WebPageSource("url", "title", testHtml)
        val publisher = chain.extract(source)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(TextContent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."))
            .expectNext(TextContent("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua."))
            .expectNext(TextContent("At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."))
            .expectNext(MetaDataContent(MetaDataType.LANGUAGE, "en"))
            .expectNext(MetaDataContent(MetaDataType.KEYWORDS, "Some, keywords, helping, achieve, semantic, web"))
            .expectNext(MetaDataContent(MetaDataType.TITLE, "Test HTML file"))
            .expectNext(MetaDataContent(MetaDataType.DESCRIPTION, "This is an HTML file for testing various content extractors. It's content may be extended when new features, extractors get added."))
            .expectNext(MetaDataContent(MetaDataType.TIMESTAMP, "2018-08-15T10:45:11+01:00"))
            .expectNext(MetaDataContent(MetaDataType.IMAGE, "https://www.pexels.com/photo/laptop-calendar-and-books-908298/"))
            .expectNext(MetaDataContent(MetaDataType.VIDEO, "https://some-video.com/"))
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com"))
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com/2"))
            .expectNext(MediaContent(MediaType.VIDEO, "http://some-stream.com/3"))
            .verifyComplete()
    }

    private fun loadHtmlFromResources(filename: String): String {
        return Jsoup.parse(File(javaClass.classLoader.getResource(filename).file), Charsets.UTF_8.name()).html()
    }
}