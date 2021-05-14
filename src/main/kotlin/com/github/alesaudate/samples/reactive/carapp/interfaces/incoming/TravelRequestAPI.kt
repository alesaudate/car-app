package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequestStatus
import com.github.alesaudate.samples.reactive.carapp.domain.TravelService
import com.github.alesaudate.samples.reactive.carapp.extensions.ISO_LOCAL_DATE_TIME_PATTERN
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.mapping.TravelRequestMapper
import org.springframework.hateoas.EntityModel
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Service
@RestController
@RequestMapping(path = ["/travel-requests"])
class TravelRequestAPI(
    val travelService: TravelService,
    val mapper: TravelRequestMapper
) {

    @PostMapping
    fun makeTravelRequest(@RequestBody @Valid travelRequestInput: TravelRequestInput) : Mono<EntityModel<TravelRequestOutput>> {

        return mapper.map(travelRequestInput)
            .flatMap { travelService.saveTravelRequest(it) }
            .flatMap { tr -> mapper.map(tr).map { tr to it  }}
            .flatMap { mapper.buildOutputModel(it.first, it.second)}

    }

    @GetMapping("/nearby")
    fun listNearbyRequests(@RequestParam currentAddress: String): Flux<EntityModel<TravelRequestOutput>> {
        return travelService.listByNearbyTravelRequests(currentAddress)
            .flatMap { tr -> mapper.map(tr).map { tr to it }}
            .flatMap { mapper.buildOutputModel(it.first, it.second) }
    }
}


@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TravelRequestInput(
    @get:NotNull
    val passengerId: Long?,

    @get:NotEmpty
    val origin: String?,

    @get:NotEmpty
    val destination: String?
)

@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TravelRequestOutput(
    val id: Long,
    val origin: String,
    val destination: String,
    val status: TravelRequestStatus,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_LOCAL_DATE_TIME_PATTERN)
    val creationDate: LocalDateTime
)