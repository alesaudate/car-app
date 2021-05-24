package com.github.alesaudate.samples.reactive.carapp.fixtures

import com.github.alesaudate.samples.reactive.carapp.domain.Passenger
import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequest
import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequestStatus
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.TravelRequestInput
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.TravelRequestOutput
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import com.github.alesaudate.samples.reactive.carapp.randomDateTimeInTheNearPast
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomValue
import java.time.LocalDateTime

fun randomTravelRequest(
    id: Long? = randomId(),
    passenger: Passenger = randomPassenger(),
    origin: String = randomAddress(),
    destination: String = randomAddress(),
    status: TravelRequestStatus = TravelRequestStatus::class.randomValue(),
    creationDate: LocalDateTime = LocalDateTime.now()
) = TravelRequest(id = id, passenger = passenger, origin = origin, destination = destination, status = status, creationDate = creationDate)

fun randomTravelRequestInput(passengerId: Long? = randomId(), origin: String? = randomAddress(), destination: String? = randomAddress()) = TravelRequestInput(passengerId = passengerId, origin = origin, destination = destination)

fun randomTravelRequestOutput(id: Long = randomId(), origin: String = randomAddress(), destination: String = randomAddress(), status: TravelRequestStatus = TravelRequestStatus::class.randomValue()) =
    TravelRequestOutput(id = id, origin = origin, destination = destination, status = status, creationDate = randomDateTimeInTheNearPast())
