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

package com.clickbait.defeater.clickbaitservice.update

import com.clickbait.defeater.clickbaitservice.update.model.SERVICE_ZONE_ID
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.SchedulerProperties
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.CacheControl
import org.springframework.scheduling.annotation.EnableScheduling
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.ZoneId
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Entry-point of the ClickBait Update-Service application.
 * Several beans are configured here that are used for dependency injection.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableWebFlux
@EnableScheduling
class ClickBaitServiceUpdateApplication : WebFluxConfigurer {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ClickBaitServiceUpdateApplication::class.java, *args)
        }
    }

    @Bean
    fun taskScheduler(): TaskScheduler {
        return ConcurrentTaskScheduler()
    }

    @Bean
    fun schedulerProperties(
        @Value("\${service.relay.votes.hours.until.now}") hours: Int,
        @Value("\${service.relay.votes.minimum.votes}") minVotes: Int
    ): SchedulerProperties {
        return SchedulerProperties(hours, minVotes)
    }

    @Bean
    fun contentExtractionServiceClient(
        @Value("\${content.service.protocol}") protocol: String,
        @Value("\${content.service.host}") host: String,
        @Value("\${content.service.port}") port: String,
        objectMapper: ObjectMapper
    ): ContentExtractionServiceClient {
        val retrofit = Retrofit.Builder()
            .baseUrl("$protocol://$host:$port/")
            .client(customOkHttpClient())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
        return retrofit.create(ContentExtractionServiceClient::class.java)
    }

    @Bean
    fun judgmentsRepository(
        @Value("\${ml.service.protocol}") protocol: String,
        @Value("\${ml.service.host}") host: String,
        @Value("\${ml.service.port}") port: String
    ): JudgmentsRepository {
        val retrofit = Retrofit.Builder()
            .baseUrl("$protocol://$host:$port/")
            .client(customOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
        return retrofit.create(JudgmentsRepository::class.java)
    }

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(KotlinModule())
        mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of(SERVICE_ZONE_ID)))
        mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        return mapper
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

    private fun customOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}