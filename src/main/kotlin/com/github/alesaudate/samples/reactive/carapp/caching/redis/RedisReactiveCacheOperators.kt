package com.github.alesaudate.samples.reactive.carapp.caching.redis

import com.github.alesaudate.samples.reactive.carapp.caching.CacheOperator
import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import liquibase.pro.packaged.it
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.core.publisher.Mono
import java.time.Duration

class RedisReactiveCacheOperators(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>) : CacheOperator {

    override operator fun <T : Any?> invoke(publisher: Mono<T>, key: String, ttl: Duration): Mono<T> {
        val ops = reactiveRedisTemplate.opsForValue()

        val result = ops.get(key).block()


        return Mono.just(key)
            .doOnNext { debug("Searching cache for content based on key {}", it) }
            .flatMap { ops.get(key) }
            .switchIfEmpty(publisher)
            .flatMap { result ->
                debug("Setting content on cache")
                ops.set(key, result as Any, ttl).map { result }
            }
            .map {   it as T }
    }
}
