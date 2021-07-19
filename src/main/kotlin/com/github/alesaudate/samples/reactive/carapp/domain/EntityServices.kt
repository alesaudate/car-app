package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

abstract class EntityService<T : Any, ID : Any>(
    private val repository: JpaRepository<T, ID>
) {

    private val scheduler = Schedulers.boundedElastic()

    fun findById(id: ID): Mono<T> {
        return Mono.fromCallable {
            repository.findByIdOrNull(id)
        }
            .flatMap { it?.let { Mono.just(it) } ?: Mono.empty() }
            .doOnNext { debug("Found entity {}", it) }
            .subscribeOn(scheduler)
    }

    fun findAll(pageable: Pageable = Pageable.unpaged()): Flux<T> {
        return Flux.fromStream { repository.findAll(pageable).get() }
            .subscribeOn(scheduler)
    }

    fun save(entity: T): Mono<T> {
        return Mono.fromCallable {
            repository.save(entity)
        }.subscribeOn(scheduler)
    }

    fun delete(entity: T): Mono<Unit> {
        return Mono.fromCallable { repository.delete(entity) }
            .subscribeOn(scheduler)
    }
}

@Service
class PassengerService(
    passengerRepository: PassengerRepository
) : EntityService<Passenger, Long> (passengerRepository)

@Service
class DriverService(
    driverRepository: DriverRepository
) : EntityService<Driver, Long> (driverRepository)
