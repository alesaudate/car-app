package com.github.alesaudate.samples.reactive.carapp.caching.redis

import com.github.alesaudate.samples.reactive.carapp.caching.CacheOperator
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.core.publisher.Mono
import java.time.Duration

class RedisReactiveCacheOperators(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>) : CacheOperator {

    override operator fun <T : Any?> invoke(publisher: Mono<T>, key: String, ttl: Duration): Mono<T> {
        val ops = reactiveRedisTemplate.opsForValue()

        return ops.get(key)
            .switchIfEmpty(
                publisher.doOnNext {
                    ops.set(key, it as Any, ttl)
                }
            )
            .map {
                it as T
            }
    }
}
