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
        return Mono.just(key)
            .doOnNext { debug("Searching cache for content based on key {}", it) }
            .flatMap { key ->
                ops.get(key).doOnNext {
                    debug("Found data for key {} in cache", key)
                }
            }
            .switchIfEmpty(publisher)
            .zipWhen {
                Mono.just(it)
                    .doOnNext { debug("Setting content on cache: {}", it) }
                    .doOnNext {
                        ops.set(key, it as Any, ttl)
                    }
            }
            .map {
                debug("Returning {}", it.t1)
                it.t1 as T
            }
    }
}
