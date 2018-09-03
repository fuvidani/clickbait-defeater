package com.clickbait.defeater.clickbaitservice.read

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.service.score.client.IScoreServiceClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import com.optimaize.langdetect.LanguageDetector
import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.data.redis.serializer.RedisSerializationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer

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
@Configuration
class ClickBaitServiceReadApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ClickBaitServiceReadApplication::class.java, *args)
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
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveStringRedisTemplate {
        return ReactiveStringRedisTemplate(factory)
    }

    @Bean
    fun reactiveJsonClickBaitScoreRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, ClickBaitScore> {
        val serializer = Jackson2JsonRedisSerializer(ClickBaitScore::class.java)
        serializer.setObjectMapper(ObjectMapper().registerModule(KotlinModule()))
        val builder = RedisSerializationContext.newSerializationContext<String, ClickBaitScore>(StringRedisSerializer())
        val serializationContext = builder.value(serializer).build()
        return ReactiveRedisTemplate<String, ClickBaitScore>(factory, serializationContext)
    }

    @Bean
    fun reactiveClickBaitScoreRedisValueOperations(redisTemplate: ReactiveRedisTemplate<String, ClickBaitScore>): ReactiveValueOperations<String, ClickBaitScore> {
        return redisTemplate.opsForValue()
    }

    @Bean
    fun scoreServiceClient(
        @Value("\${score.service.protocol}") protocol: String,
        @Value("\${score.service.host}") host: String,
        @Value("\${score.service.port}") port: String
    ): IScoreServiceClient {
        val retrofit = Retrofit.Builder()
            .baseUrl("$protocol://$host:$port/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
        return retrofit.create(IScoreServiceClient::class.java)
    }

    @Bean
    fun languageDetector(@Value("\${languages}") languages: Array<String>): LanguageDetector {
        val languageProfiles = if (languages[0] == "all") {
            LanguageProfileReader().readAllBuiltIn()
        } else {
            LanguageProfileReader().read(languages.asList())
        }
        return LanguageDetectorBuilder.create(NgramExtractors.standard())
            .withProfiles(languageProfiles)
            .build()
    }

    @Bean
    fun supportedLanguages(@Value("\${supported.languages}") supportedLanguages: Array<String>): List<String> {
        return supportedLanguages.asList()
    }
}