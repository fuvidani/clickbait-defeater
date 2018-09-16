package com.clickbait.defeater.gateway

import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64

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
        @Value("\${security.auth.encoded}") gatewayAuth: String,
        @Value("\${security.authorized.hosts.pattern}") authorizedHostsPattern: Regex,
        passwordEncoder: PasswordEncoder,
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
                    accept()
                } else {
                    val authorization = context.exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
                    if (authorization == null || !authorization.startsWith("Basic ") || !passwordEncoder.matches(
                            getPlainPassword(authorization),
                            gatewayAuth
                        )
                    ) {
                        logger.warn("No authorization header or invalid credentials provided (Host: ${address?.hostString})")
                        deny()
                    } else {
                        accept()
                    }
                }
            }

        return http.build()
    }

    private fun deny(): Mono<AuthorizationDecision> {
        return Mono.just(AuthorizationDecision(false))
    }

    private fun accept(): Mono<AuthorizationDecision> {
        return Mono.just(AuthorizationDecision(true))
    }

    private fun getPlainPassword(authorization: String): String {
        val base64Credentials = authorization.substring(6).trim()
        val credDecoded = Base64.getDecoder().decode(base64Credentials)
        val credentials = String(credDecoded, StandardCharsets.UTF_8)
        // credentials = username:password
        return credentials.split(":".toRegex(), 2)[1]
    }

    @Bean(name = ["passwordEncoder"])
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder(5, SecureRandom())
    }

    companion object : KLogging()
}