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
 * Centralized security configuration for the gateway.
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
     * disables form login and accepts requests only if
     * (i) the requesting host is from an injected list of authorized hosts (i.e. it's
     * a case of intra-service communication) OR
     * (ii) there is a Basic authentication header with the correct password of the gateway.
     *
     * @param authorizedHostsPattern a regex pattern describing from which hosts (IP-address patterns)
     * requests should be accepted
     * @param http [ServerHttpSecurity] bean from the Spring Framework
     */
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
                        logger.warn("No authorization header or invalid credentials provided (Address: $address, Host: ${address?.hostString})")
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