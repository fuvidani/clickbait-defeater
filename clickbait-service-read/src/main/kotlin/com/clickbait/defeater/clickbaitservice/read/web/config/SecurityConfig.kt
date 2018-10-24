package com.clickbait.defeater.clickbaitservice.read.web.config

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
 * Centralized security configuration for this service.
 * Only enabled if the current profile is not `test`.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableWebFluxSecurity
@Configuration
@Profile("!test")
class SecurityConfig {

    /**
     * Custom [SecurityWebFilterChain]. Denies all requests on the `/actuator/` paths,
     * disables form login and accepts requests only from an injected list of authorzied
     * hosts.
     *
     * @param authorizedHostsPattern a regex pattern describing from which hosts (IP-address patterns)
     * requests should be accepted
     * @param http [ServerHttpSecurity] bean from the Spring Framework
     */
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
            .pathMatchers("/actuator/**")
            .denyAll()
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