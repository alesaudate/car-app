package com.github.alesaudate.samples.reactive.carapp.caching.redis

import com.github.alesaudate.samples.reactive.carapp.randomDouble
import com.github.alesaudate.samples.reactive.carapp.randomName
import com.github.alesaudate.samples.reactive.carapp.randomPositiveInt
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest(classes = [RedisReactiveCacheOperators::class])
internal class RedisReactiveCacheOperatorsUnitTest {

    @Autowired
    lateinit var redisReactiveCacheOperators: RedisReactiveCacheOperators

    @MockkBean
    lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>

    @MockkBean
    lateinit var ops: ReactiveValueOperations<String, Any>

    @BeforeEach
    fun setup() {
        every { reactiveRedisTemplate.opsForValue() } returns ops
    }

    @Test
    fun `given that a value is present in cache for some key, when I invoke the cache operator then the value is returned from cache instead of from the operation`() {

        // arrange
        val randomValue = randomDouble()
        val cachedRandomValue = randomDouble()
        val randomKey = randomName()
        val actualMono = Mono.just(randomValue)
        every { ops.get(randomKey) } returns Mono.just(cachedRandomValue)
        every { ops.set(any(), any(), ofType(Duration::class)) } returns Mono.just(true)

        // act
        val resultingValue = redisReactiveCacheOperators.invoke(
            actualMono,
            randomKey,
            Duration.ofSeconds(randomPositiveInt(until = 1000).toLong())
        )
            .block()!!

        // assert
        assertEquals(cachedRandomValue, resultingValue)
        verify(exactly = 1) { ops.get(randomKey) }
        StepVerifier.create(actualMono).expectNoEvent(Duration.ofSeconds(1))
    }

    @Test
    fun `given that no value is present in cache for some key, when I invoke the cache operator then the value is returned from the operation instead of from the cache`() {

        // arrange
        val randomValue = randomDouble()
        val randomKey = randomName()
        val actualMono = Mono.just(randomValue)
        val duration = Duration.ofSeconds(randomPositiveInt(until = 1000).toLong())
        every { ops.get(randomKey) } returns Mono.empty()
        every { ops.set(any(), any(), ofType(Duration::class)) } returns Mono.just(true)

        // act
        val resultingValue = redisReactiveCacheOperators.invoke(
            actualMono,
            randomKey,
            duration
        )
            .block()!!

        // assert
        assertEquals(randomValue, resultingValue)
        verify { ops.get(randomKey) }
        verify { ops.set(randomKey, randomValue, duration) }
        StepVerifier.create(actualMono).expectSubscription()
    }

    @Test
    fun `given that no value is present in cache for some key, when I invoke the cache operator then the value is returned even if it the cache set operation returns false`() {

        // arrange
        val randomValue = randomDouble()
        val randomKey = randomName()
        val actualMono = Mono.just(randomValue)
        val duration = Duration.ofSeconds(randomPositiveInt(until = 1000).toLong())
        every { ops.get(randomKey) } returns Mono.empty()
        every { ops.set(any(), any(), ofType(Duration::class)) } returns Mono.just(false)

        // act
        val resultingValue = redisReactiveCacheOperators.invoke(
            actualMono,
            randomKey,
            duration
        )
            .block()!!

        // assert
        assertEquals(randomValue, resultingValue)
        verify { ops.get(randomKey) }
        verify { ops.set(randomKey, randomValue, duration) }
        StepVerifier.create(actualMono).expectSubscription()
    }

    @Test
    fun `given that no value is present in cache for some key, when I invoke the cache operator then the value is returned even if it the cache set operation returns empty`() {

        // arrange
        val randomValue = randomDouble()
        val randomKey = randomName()
        val actualMono = Mono.just(randomValue)
        val duration = Duration.ofSeconds(randomPositiveInt(until = 1000).toLong())
        every { ops.get(randomKey) } returns Mono.empty()
        every { ops.set(any(), any(), ofType(Duration::class)) } returns Mono.empty()

        // act
        val resultingValue = redisReactiveCacheOperators.invoke(
            actualMono,
            randomKey,
            duration
        ).block()!!

        // assert
        assertEquals(randomValue, resultingValue)
        verify { ops.get(randomKey) }
        verify { ops.set(randomKey, randomValue, duration) }
        StepVerifier.create(actualMono).expectSubscription()
    }
}
