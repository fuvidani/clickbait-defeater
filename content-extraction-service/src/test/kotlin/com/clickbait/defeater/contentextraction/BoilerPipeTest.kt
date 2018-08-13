package com.clickbait.defeater.contentextraction

import com.clickbait.defeater.contentextraction.model.WebPageSource
import com.clickbait.defeater.contentextraction.service.extractor.extractors.BoilerPipeExtractor
import com.clickbait.defeater.contentextraction.service.extractor.DefaultExtractorChain
import com.clickbait.defeater.contentextraction.service.extractor.extractors.HtmlExtractor
import com.clickbait.defeater.contentextraction.service.extractor.extractors.JsoupVideoExtractor
import com.kohlschutter.boilerpipe.extractors.CommonExtractors
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter
import com.kohlschutter.boilerpipe.sax.ImageExtractor
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.net.URL

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
@SpringBootTest(value = ["application.yml"], classes = [ContentExtractionServiceApplication::class])
class BoilerPipeTest {

    @Test
    fun testThis() {
        // val url = URL("http://www.sportbible.com/football/news-gonzalo-higuain-scores-sublime-goal-for-ac-milan-against-real-madrid-20180811")
        val url = URL("https://index.hu/sport/futball/2018/08/11/andres_iniesta_japan_gol_csel/")
        val extractor = CommonExtractors.ARTICLE_EXTRACTOR
        val text = extractor.getText(url)

        text.split("\n").forEach { println(it) }
        // println(extractor.getText(url))
        val imageExtractor = ImageExtractor.INSTANCE
        val images = imageExtractor.process(url, extractor)

        images.forEach { println(it) }

        val doc = Jsoup.connect(url.toString()).get()
        println(doc.title())
        val elements = doc.getElementsByTag("iframe")
        val filtered = elements.filter { element ->
            var height = element.attr("height")
            var width = element.attr("width")
            if (height.isNotEmpty() && width.isNotEmpty()) {
                if (height.endsWith("%")) {
                    height = height.dropLast(1)
                }
                if (width.endsWith("%")) {
                    width = width.dropLast(1)
                }
                height.toInt() > 0 && width.toInt() > 0
            } else {
                false
            }
        }
        filtered.forEach { println(it.attr("src")) }

        val smallDoc = Jsoup.parse(HTMLHighlighter.newExtractingInstance().process(url, extractor))
        println(smallDoc)
    }

    @Test
    fun extractorChainTest() {
        val chain = DefaultExtractorChain(
            listOf(
                HtmlExtractor(),
                BoilerPipeExtractor(),
                JsoupVideoExtractor()
            )
        )
        val publisher = chain.extract(
            WebPageSource(
                "http://www.nemzetisport.hu/sportszelep/nagy-hirt-jelentettek-be-mutina-agnesek-kep-2652655",
                "Title",
                ""
            )
        )
        /*StepVerifier.create(publisher)
            .expectSubscription()
            .expectNextMatches {  }*/
        publisher.doOnNext { println(it) }.subscribe()
    }
}