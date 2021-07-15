package com.github.alesaudate.samples.reactive.carapp.caching

import reactor.core.publisher.Mono
import java.time.Duration

interface CacheOperator {

    operator fun <T : Any?> invoke(publisher: Mono<T>, key: String, ttl: Duration): Mono<T>
}
