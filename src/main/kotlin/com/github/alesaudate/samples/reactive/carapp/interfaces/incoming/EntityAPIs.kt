package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.alesaudate.samples.reactive.carapp.domain.*
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling.EntityNotFoundException
import org.springframework.hateoas.server.core.LastInvocationAware
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Size

abstract class EntityAPI<T, ID, P>(
    val entityService: EntityService<T, ID>
) {


    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE])
    fun listAll() = entityService.findAll()

    @GetMapping("/{id}")
    fun findSingle(@PathVariable("id") id: ID): Mono<T> =
        entityService.findById(id)
            .switchIfEmpty { throw entityNotFoundException() }

    @PostMapping
    fun create(@RequestBody entity: T) = entityService.save(entity)

    @PutMapping("/{id}")
    fun fullUpdate(@PathVariable("id") id: ID, @RequestBody @Valid entity: T) : Mono<T> {

        return findSingle(id)
            .map { it copyFrom entity}
            .flatMap { entityService.save(it) }
    }

    @PatchMapping("/{id}")
    fun incrementalUpdatePassenger(@PathVariable("id") id: ID, @RequestBody @Valid patchEntity: P): Mono<T> {
        return findSingle(id)
            .map { it absorbFrom patchEntity }
            .flatMap { entityService.save(it) }
    }

    @DeleteMapping("/{id}")
    fun deletePassenger(@PathVariable("id") id: ID) = findSingle(id).flatMap { entityService.delete(it) }

    abstract fun entityNotFoundException() : EntityNotFoundException

    abstract infix fun T.copyFrom(entity: T) : T

    abstract infix fun T.absorbFrom(patch: P) : T
}


@Service
@RestController
@RequestMapping(path = ["/passengers"])
class PassengerAPI(
    val passengerService: PassengerService
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
    val driverService: DriverService
): EntityAPI<Driver, Long, PatchDriver>(driverService) {

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