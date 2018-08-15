package com.clickbait.defeater.contentextraction

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.service.html.extractor.DefaultExtractorChain
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.*
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.metadata.JsoupMetaDataExtractor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

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
    fun reactiveJsonContentRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, ContentWrapper> {
        val serializer = Jackson2JsonRedisSerializer(ContentWrapper::class.java)
        serializer.setObjectMapper(ObjectMapper().registerModule(KotlinModule()))
        val builder = RedisSerializationContext.newSerializationContext<String, ContentWrapper>(StringRedisSerializer())
        val serializationContext = builder.value(serializer).build()
        return ReactiveRedisTemplate<String, ContentWrapper>(factory, serializationContext)
    }

    @Bean
    fun reactiveContentsRedisValueOperations(redisTemplate: ReactiveRedisTemplate<String, ContentWrapper>): ReactiveValueOperations<String, ContentWrapper> {
        return redisTemplate.opsForValue()
    }

    @Bean
    fun extractorOrder(): List<Extractor> {
        return listOf(
            BoilerPipeTextExtractor(),
            BoilerPipeImageExtractor(),
            JsoupMetaDataExtractor(),
            JsoupVideoExtractor()
        )
    }

    @Bean
    fun extractorChain(extractorOrder: List<Extractor>): ExtractorChain {
        return DefaultExtractorChain(extractorOrder, 0)
    }
}