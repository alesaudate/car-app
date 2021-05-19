package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.mapping

import com.github.alesaudate.samples.reactive.carapp.domain.PassengerService
import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequest
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.TravelRequestInput
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.TravelRequestOutput
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling.EntityNotFoundException
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class TravelRequestMapper(
    val passengerService: PassengerService
) {

    fun map(input: TravelRequestInput): Mono<TravelRequest> {

        return passengerService.findById(input.passengerId!!)
            .switchIfEmpty { throw EntityNotFoundException("passenger") }
            .map { TravelRequest(passenger = it, origin = input.origin!!, destination = input.destination!!) }
    }

    fun map(travelRequest: TravelRequest): Mono<TravelRequestOutput> {
        return Mono.just(
            TravelRequestOutput(
                id = travelRequest.id!!,
                origin = travelRequest.origin,
                destination = travelRequest.destination,
                status = travelRequest.status,
                creationDate = travelRequest.creationDate
            )
        )
    }

    fun buildOutputModel(travelRequest: TravelRequest, output: TravelRequestOutput): Mono<EntityModel<TravelRequestOutput>> {

        val link = Link.of("/passengers/${travelRequest.passenger.id}", "passenger")
        return Mono.just(EntityModel.of(output, link))
    }
}
