package com.clickbait.defeater.gateway

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono

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
@EnableCircuitBreaker
@RestController
class GatewayApplication : WebFluxConfigurer {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(GatewayApplication::class.java, *args)
        }
    }

    @RequestMapping("/clickBaitReadFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun clickBaitReadServiceFallback(): Mono<String> {
        return Mono.just("Fallback for ClickBait-Read-Service")
    }

    @RequestMapping("/clickBaitUpdateFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun clickBaitUpdateServiceFallback(): Mono<String> {
        return Mono.just("Fallback for ClickBait-Update-Service")
    }

    @RequestMapping("/contentExtractionFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun contentExtractionServiceFallback(): Mono<String> {
        return Mono.just("Fallback for Content-Extraction-Service")
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
