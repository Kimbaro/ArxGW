package com.esg.team.esg.config

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Component
class CorsConfig {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        corsConfig.allowedOrigins = listOf("http://localhost:3000", "http://192.168.0.76:3000");
        corsConfig.maxAge = 8000L
        corsConfig.addAllowedMethod(HttpMethod.GET)
        corsConfig.addAllowedMethod(HttpMethod.POST)
        corsConfig.addAllowedMethod(HttpMethod.DELETE)
        corsConfig.addAllowedMethod(HttpMethod.PUT)
        corsConfig.addAllowedMethod(HttpMethod.OPTIONS)
        corsConfig.addAllowedHeader("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)

        return CorsWebFilter(source)
    }
}