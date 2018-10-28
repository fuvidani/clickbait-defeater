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

package com.clickbait.defeater.gateway

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono

/**
 * Entry-point of the ClickBait Gateway application.
 * Several beans are configured here that are used for dependency injection.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableWebFlux
@EnableCircuitBreaker
@RestController
class GatewayApplication : WebFluxConfigurer {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(GatewayApplication::class.java, *args)
        }
    }

    /**
     * Simple plain fallback response in case the gateway cannot reach the
     * Read-Service.
     */
    @RequestMapping("/clickBaitReadFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun clickBaitReadServiceFallback(): Mono<String> {
        return Mono.just("Fallback for ClickBait-Read-Service")
    }

    /**
     * Simple plain fallback response in case the gateway cannot reach the
     * Update-Service.
     */
    @RequestMapping("/clickBaitUpdateFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun clickBaitUpdateServiceFallback(): Mono<String> {
        return Mono.just("Fallback for ClickBait-Update-Service")
    }

    /**
     * Simple plain fallback response in case the gateway cannot reach the
     * Content-Extraction-Service.
     */
    @RequestMapping("/contentExtractionFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun contentExtractionServiceFallback(): Mono<String> {
        return Mono.just("Fallback for Content-Extraction-Service")
    }

    /**
     * Simple plain fallback response in case the gateway cannot reach the
     * ML-Service.
     */
    @RequestMapping("/mlServiceFallback", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun contentMlServiceFallback(): Mono<String> {
        return Mono.just("Fallback for ML-Service")
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
