package com.clickbait.defeater.contentextraction.service.html.extractor.extractors

import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import org.jsoup.Jsoup
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
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
abstract class AbstractExtractorTest(protected val extractor: Extractor) {

    @MockBean
    protected lateinit var chain: ExtractorChain
    protected val testHtml: String = loadHtmlFromResources("test_html.html")
    protected val realHtml: String = loadHtmlFromResources("real_article.html")

    @Before
    open fun setUp() {
        Mockito.`when`(chain.extract(any(WebPageSource::class.java))).thenReturn(Flux.empty())
    }

    private fun loadHtmlFromResources(filename: String): String {
        return Jsoup.parse(File(javaClass.classLoader.getResource(filename).file), Charsets.UTF_8.name()).html()
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}