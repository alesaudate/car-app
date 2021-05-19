package com.github.alesaudate.samples.reactive.carapp.fixtures

import com.github.alesaudate.samples.reactive.carapp.domain.Passenger
import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequest
import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequestStatus
import com.github.alesaudate.samples.reactive.carapp.random
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import com.github.alesaudate.samples.reactive.carapp.randomId
import java.time.LocalDateTime

fun randomTravelRequest(id: Long? = randomId(), passenger: Passenger = randomPassenger(), origin: String = randomAddress(), destination: String = randomAddress(), status: TravelRequestStatus = TravelRequestStatus::class.random(), creationDate: LocalDateTime = LocalDateTime.now()) = TravelRequest(id = id, passenger = passenger, origin = origin, destination = destination, status = status, creationDate = creationDate)
