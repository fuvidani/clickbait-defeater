/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.clickbaitservice.read

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.service.score.client.ScoreServiceClient
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
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 * Entry-point of the ClickBait Read-Service application.
 * Several beans are configured here that are used for dependency injection.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableWebFlux
@Configuration
class ClickBaitServiceReadApplication : WebFluxConfigurer {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ClickBaitServiceReadApplication::class.java, *args)
        }
    }

    /**
     * Specifies the [ReactiveRedisConnectionFactory] this applications uses.
     *
     * @param host host of the Redis cache
     * @param port port of the Redis cache
     * @param password password for accessing the Redis cache
     * @return a [ReactiveRedisConnectionFactory] bean
     */
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

    /**
     * Retrofit HTTP client implementation of the [ScoreServiceClient] interface as
     * a bean.
     *
     * @param protocol protocol to use, either `http` or `https`
     * @param host host of the remote API where the score requests can be invoked
     * @param port port of the remote API where the score requests can be invoked
     */
    @Bean
    fun scoreServiceClient(
        @Value("\${score.service.protocol}") protocol: String,
        @Value("\${score.service.host}") host: String,
        @Value("\${score.service.port}") port: String
    ): ScoreServiceClient {
        val retrofit = Retrofit.Builder()
            .baseUrl("$protocol://$host:$port/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
        return retrofit.create(ScoreServiceClient::class.java)
    }

    /**
     * Bean implementation for [com.optimaize.langdetect.LanguageDetector] library interface.
     *
     * @param languages list of ISO-639-1 language codes the language detector should search for
     */
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

    /**
     * @param supportedLanguages list of ISO-639-1 language codes this application supports
     */
    @Bean
    fun supportedLanguages(@Value("\${supported.languages}") supportedLanguages: Array<String>): List<String> {
        return supportedLanguages.asList()
    }

    /**
     * Add resource handlers for serving static resources.
     * @see ResourceHandlerRegistry
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/documentation/**")
            .addResourceLocations("classpath:/static/docs/")
            .setCacheControl(CacheControl.noStore())
    }
}