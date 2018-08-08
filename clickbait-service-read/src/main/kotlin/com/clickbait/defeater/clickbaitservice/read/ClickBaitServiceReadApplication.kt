package com.clickbait.defeater.clickbaitservice.read

import com.clickbait.defeater.clickbaitservice.read.service.score.client.IScoreServiceClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
        @Value("\${spring.redis.sentinel.master}") master: String,
        @Value("\${spring.redis.sentinel.nodes}") nodes: Set<String>,
        @Value("\${spring.redis.password}") password: String
    ): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(RedisSentinelConfiguration(master, nodes))
    }

    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveStringRedisTemplate {
        return ReactiveStringRedisTemplate(factory)
    }

    @Bean
    fun scoreServiceClient(
        @Value("\${score.service.host}") host: String,
        @Value("\${score.service.port}") port: String
    ): IScoreServiceClient {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$host:$port/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(IScoreServiceClient::class.java)
    }
}