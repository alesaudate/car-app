package com.github.alesaudate.samples.reactive.carapp.caching.redis

import com.github.alesaudate.samples.reactive.carapp.caching.CacheOperator
import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import liquibase.pro.packaged.it
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import java.time.Duration

class RedisReactiveCacheOperators(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>) : CacheOperator {

    override operator fun <T : Any?> invoke(publisher: Mono<T>, key: String, ttl: Duration): Mono<T> {
        val ops = reactiveRedisTemplate.opsForValue()
        return Mono.just(key)
            .doOnNext { debug("Searching cache for content based on key {}", it) }
            .flatMap { k ->
                ops.get(k).doOnNext {
                    debug("Found data for key {} in cache", k)
                }
            }
            .switchIfEmpty(publisher.flatMap {
                setDataOnCache(it, key, ttl, ops)
            })
            .map {
                debug("Returning {}", it)
                it as T
            }
    }

    private fun <T: Any?> setDataOnCache(v: T, key: String, ttl: Duration,  ops: ReactiveValueOperations<String, T>): Mono<T> {
        debug("Setting content on cache: {}", v as Any)
        return ops.set(key, v, ttl).map { v }
    }
}
