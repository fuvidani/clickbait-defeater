package com.clickbait.defeater.clickbaitservice.update

import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.SchedulerProperties
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.TaskScheduler
import retrofit2.converter.jackson.JacksonConverterFactory

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
@EnableScheduling
class ClickBaitServiceUpdateApplication {

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
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
        return retrofit.create(JudgmentsRepository::class.java)
    }
}