package com.github.alesaudate.samples.reactive.carapp.config

import com.github.alesaudate.samples.reactive.carapp.caching.CacheOperator
import com.github.alesaudate.samples.reactive.carapp.caching.redis.RedisReactiveCacheOperators
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class CacheConfig {

    @Bean
    @Primary
    fun <T : Any?> cacheOperator(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): CacheOperator {

        val valueSerializer = JdkSerializationRedisSerializer()

        val serializationContextBuilder = RedisSerializationContext.newSerializationContext<String, Any>(StringRedisSerializer())
        val serializationContext = serializationContextBuilder.value(valueSerializer).build()

        val reactiveRedisTemplate = ReactiveRedisTemplate(reactiveRedisConnectionFactory, serializationContext)

        return RedisReactiveCacheOperators(reactiveRedisTemplate)
    }
}
