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

package com.clickbait.defeater.contentextraction

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.service.html.extractor.DefaultExtractorChain
import com.clickbait.defeater.contentextraction.service.html.extractor.Extractor
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorBean
import com.clickbait.defeater.contentextraction.service.html.extractor.ExtractorChain
import com.clickbait.defeater.contentextraction.service.html.extractor.extractors.mercury.web.parser.client.MercuryWebParserApiClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Entry-point of the ClickBait Content-Extraction-Service application.
 * Several beans are configured here that are used for dependency injection.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableWebFlux
class ContentExtractionServiceApplication : WebFluxConfigurer {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ContentExtractionServiceApplication::class.java, *args)
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

    /**
     * Instantiates a [DefaultExtractorChain] with the annotation-based
     * [Extractor] implementations respecting their order-value.
     * Provides an [ExtractorChain] bean for the application context.
     */
    @Bean
    fun extractors(context: ApplicationContext): ExtractorChain {
        val beans = context.getBeansWithAnnotation(ExtractorBean::class.java)
        val extractors = beans.values
            .filter { it is Extractor }
            .map { it as Extractor }
            .sortedBy { it.javaClass.getAnnotation(ExtractorBean::class.java).order }
        return DefaultExtractorChain(extractors)
    }

    @Bean
    fun mercuryWebParserApiClient(): MercuryWebParserApiClient {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://mercury.postlight.com/")
            .client(unsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
        return retrofit.create(MercuryWebParserApiClient::class.java)
    }

    /**
     * Custom [OkHttpClient] instance which doesn't check for certificates
     * in order to seamlessly invoke the Mercury Web Parser API using Retrofit.
     */
    private fun unsafeOkHttpClient(): OkHttpClient {
        val trustManager = arrayOf<TrustManager>(object : X509TrustManager {

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustManager, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
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