package com.github.alesaudate.samples.reactive.carapp.extensions

import com.github.alesaudate.samples.reactive.carapp.caching.CacheOperator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class CacheOperatorMock() : CacheOperator {
    override fun <T> invoke(publisher: Mono<T>, key: String, ttl: Duration): Mono<T> {
        return publisher
    }
}
