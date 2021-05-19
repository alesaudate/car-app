package com.github.alesaudate.samples.reactive.carapp.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig() {

    @Bean
    @Qualifier("GMaps")
    fun webClientBuilderGMaps() = WebClient.builder()
}
