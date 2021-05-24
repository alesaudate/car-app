package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.mapping

import com.github.alesaudate.samples.reactive.carapp.domain.PassengerService
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomPassenger
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomTravelRequest
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomTravelRequestInput
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling.EntityNotFoundException
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono

@SpringBootTest(classes = [TravelRequestMapper::class])
class TravelRequestMapperUnitTest {

    @Autowired
    lateinit var travelRequestMapper: TravelRequestMapper

    @MockkBean
    lateinit var passengerService: PassengerService

    @Test
    fun `given a travel request input, when I map it, then I should get a valid travel request entity`() {

        val travelRequestInput = randomTravelRequestInput()
        val passenger = randomPassenger()
        every { passengerService.findById(travelRequestInput.passengerId!!) } returns Mono.just(passenger)

        val travelRequest = travelRequestMapper.map(travelRequestInput).block()!!

        assertEquals(travelRequestInput.origin, travelRequest.origin)
        assertEquals(travelRequestInput.destination, travelRequest.destination)
        assertEquals(passenger, travelRequest.passenger)
    }

    @Test
    fun `given a travel request input with an invalid passenger id, when I map it, then I should get an exception`() {

        val travelRequestInput = randomTravelRequestInput()

        every { passengerService.findById(any()) } returns Mono.empty()

        assertThrows<EntityNotFoundException> { travelRequestMapper.map(travelRequestInput).block() }
    }

    @Test
    fun `given a travel request, when I map it, then I should get a travel request output`() {

        val travelRequest = randomTravelRequest()
        val travelRequestOutput = travelRequestMapper.map(travelRequest).block()!!

        assertEquals(travelRequest.id, travelRequestOutput.id)
        assertEquals(travelRequest.origin, travelRequestOutput.origin)
        assertEquals(travelRequest.destination, travelRequestOutput.destination)
        assertEquals(travelRequest.status, travelRequestOutput.status)
        assertEquals(travelRequest.creationDate, travelRequestOutput.creationDate)
    }
}
