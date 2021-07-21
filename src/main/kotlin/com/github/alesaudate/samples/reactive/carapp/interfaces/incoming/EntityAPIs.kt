package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.alesaudate.samples.reactive.carapp.domain.Driver
import com.github.alesaudate.samples.reactive.carapp.domain.DriverService
import com.github.alesaudate.samples.reactive.carapp.domain.EntityService
import com.github.alesaudate.samples.reactive.carapp.domain.Passenger
import com.github.alesaudate.samples.reactive.carapp.domain.PassengerService
import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling.EntityNotFoundException
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Size

abstract class EntityAPI<T : Any, ID : Any, P : Any>(
    private val entityService: EntityService<T, ID>
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE])
    fun listAll() = entityService.findAll()

    @GetMapping("/{id}")
    fun findSingle(@PathVariable("id") id: ID): Mono<T> = Mono.just(id)
        .doOnNext { debug("Finding entity identified by {}", it) }
        .flatMap { entityService.findById(id) }
        .doOnNext { debug("Found entity identified by {}", it) }
        .switchIfEmpty { throw entityNotFoundException() }

    @PostMapping
    fun create(@RequestBody entity: T) = Mono.just(entity)
        .doOnNext { debug("Creating entity {}", entity) }
        .flatMap { entityService.save(it) }
        .doOnNext { debug("Created entity {}", entity) }

    @PutMapping("/{id}")
    fun fullUpdate(@PathVariable("id") id: ID, @RequestBody @Valid entity: T): Mono<T> {

        return findSingle(id)
            .doOnNext { debug("About to update entity {}", it) }
            .map { it copyFrom entity }
            .flatMap { entityService.save(it) }
            .doOnNext { debug("Updated entity {}", it) }
    }

    @PatchMapping("/{id}")
    fun incrementalUpdate(@PathVariable("id") id: ID, @RequestBody @Valid patchEntity: P): Mono<T> {
        return findSingle(id)
            .map { it absorbFrom patchEntity }
            .flatMap { entityService.save(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteEntity(@PathVariable("id") id: ID) = findSingle(id).flatMap { entityService.delete(it) }

    abstract fun entityNotFoundException(): EntityNotFoundException

    abstract infix fun T.copyFrom(entity: T): T

    abstract infix fun T.absorbFrom(patch: P): T
}

@Service
@RestController
@RequestMapping(path = ["/passengers"])
class PassengerAPI(
    passengerService: PassengerService
) : EntityAPI<Passenger, Long, PatchPassenger>(passengerService) {

    override fun entityNotFoundException() = EntityNotFoundException("passenger")

    override fun Passenger.copyFrom(entity: Passenger) = this.copy(name = entity.name)

    override fun Passenger.absorbFrom(patch: PatchPassenger) = this.copy(name = patch.name ?: this.name)
}

data class PatchPassenger(
    val name: String?
)

@Service
@RestController
@RequestMapping(path = ["/drivers"])
class DriverAPI(
    driverService: DriverService
) : EntityAPI<Driver, Long, PatchDriver>(driverService) {

    override fun entityNotFoundException() = EntityNotFoundException("driver")

    override fun Driver.copyFrom(entity: Driver) = this.copy(name = entity.name, birthDate = entity.birthDate)

    override fun Driver.absorbFrom(patch: PatchDriver) = this.copy(name = patch.name ?: this.name, birthDate = patch.birthDate ?: this.birthDate)
}

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PatchDriver(

    @get:Size(min = 5, max = 255)
    val name: String?,
    val birthDate: LocalDate?
)
