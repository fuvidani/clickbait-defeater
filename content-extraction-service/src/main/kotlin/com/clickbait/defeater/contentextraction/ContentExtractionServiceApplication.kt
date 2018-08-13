package com.clickbait.defeater.contentextraction

import com.clickbait.defeater.contentextraction.service.extractor.DefaultExtractorChain
import com.clickbait.defeater.contentextraction.service.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.extractor.ExtractorChain
import com.clickbait.defeater.contentextraction.service.extractor.extractors.BoilerPipeImageExtractor
import com.clickbait.defeater.contentextraction.service.extractor.extractors.BoilerPipeTextExtractor
import com.clickbait.defeater.contentextraction.service.extractor.extractors.HtmlExtractor
import com.clickbait.defeater.contentextraction.service.extractor.extractors.JsoupVideoExtractor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
class ContentExtractionServiceApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ContentExtractionServiceApplication::class.java, *args)
        }
    }

    @Bean
    @Primary
    fun redisConnectionFactory(
        @Value("\${spring.redis.host}") host: String,
        @Value("\${spring.redis.port}") port: Int,
        @Value("\${spring.redis.password}") password: String
    ): ReactiveRedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration(host, port)
        configuration.password = RedisPassword.of(password)
        return LettuceConnectionFactory(configuration)
    }

    @Bean
    fun extractorOrder(): List<Extractor> {
        return listOf(
            HtmlExtractor(),
            BoilerPipeTextExtractor(),
            BoilerPipeImageExtractor(),
            JsoupVideoExtractor()
        )
    }

    @Bean
    fun extractorChain(extractorOrder: List<Extractor>): ExtractorChain {
        return DefaultExtractorChain(extractorOrder, 0)
    }
}