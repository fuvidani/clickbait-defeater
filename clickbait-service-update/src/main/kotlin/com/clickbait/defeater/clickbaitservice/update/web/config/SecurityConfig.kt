package com.clickbait.defeater.clickbaitservice.update.web.config

import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
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
@EnableWebFluxSecurity
@Configuration
@Profile("!test")
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        @Value("\${security.authorized.hosts.pattern}") authorizedHostsPattern: Regex,
        http: ServerHttpSecurity
    ): SecurityWebFilterChain {
        http.httpBasic()
            .disable()
            .formLogin()
            .disable()
            .csrf()
            .disable()
            .authorizeExchange()
            .anyExchange()
            .access { _, context ->
                val address = context.exchange.request.remoteAddress
                if (address != null && address.hostString.matches(authorizedHostsPattern)) {
                    Mono.just(AuthorizationDecision(true))
                } else {
                    logger.warn("Request from unauthorized host (${address?.hostString})")
                    Mono.just(AuthorizationDecision(false))
                }
            }

        return http.build()
    }

    companion object : KLogging()
}