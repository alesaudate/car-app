package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.github.alesaudate.samples.reactive.carapp.domain.PassengerService
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomPassenger
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling.EntityNotFoundException
import com.github.alesaudate.samples.reactive.carapp.listOfData
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomName
import com.github.alesaudate.samples.reactive.carapp.randomPositiveInt
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest(classes = [PassengerAPI::class])
class PassengerAPIUnitTest {

    @MockkBean
    lateinit var passengerService: PassengerService

    @Autowired
    lateinit var passengerAPI: PassengerAPI

    @Test
    fun `given a passenger API, when I request a list of all passengers, then I should get the list`() {
        val listOfPassengers = listOfData(randomPositiveInt(until = 100)) { randomPassenger() }
        every { passengerService.findAll(any()) } returns Flux.fromIterable(listOfPassengers)
        val fluxOfData = passengerAPI.listAll()
        StepVerifier.create(fluxOfData).expectNextCount(listOfPassengers.size.toLong()).verifyComplete()
    }

    @Test
    fun `given that I have a registered passenger, when I request this passenger using his ID, then I should retrieve him successfully`() {
        val passenger = randomPassenger()
        every { passengerService.findById(passenger.id!!) } returns Mono.just(passenger)
        val foundPassenger = passengerAPI.findSingle(passenger.id!!).block()
        Assertions.assertEquals(passenger, foundPassenger)
    }

    @Test
    fun `given that I do not have a registered passenger, when I request a passenger using some ID, then I should get an exception`() {

        val id = randomId()
        every { passengerService.findById(id) } returns Mono.empty()
        assertThrows<EntityNotFoundException> { passengerAPI.findSingle(id).block() }
    }

    @Test
    fun `given a passenger API, when I request to create a passenger, then it is created`() {

        val passenger = randomPassenger(id = null)
        val id = randomId()
        every { passengerService.save(passenger) } returns Mono.just(passenger.copy(id = id))

        val returnedPassenger = passengerAPI.create(passenger).block()
        assertEquals(passenger.copy(id = id), returnedPassenger)
    }

    @Test
    fun `given that I have a registered passenger, when I request to fully update using this passenger name, then he should be updated`() {

        val passenger = randomPassenger()
        val updatePassenger = passenger.copy(id = null, name = randomName())
        val id = passenger.id!!

        every { passengerService.findById(id) } returns Mono.just(passenger)
        every { passengerService.save(any()) } answers { Mono.just(firstArg()) }
        val updatedPassenger = passengerAPI.fullUpdate(id, updatePassenger).block()
        assertEquals(updatePassenger.copy(id = id), updatedPassenger)
    }

    @Test
    fun `given that I do not have any registered passengers, when I request to update some passenger, then I should get an exception`() {

        val passenger = randomPassenger()
        every { passengerService.findById(passenger.id!!) } returns Mono.empty()

        assertThrows<EntityNotFoundException> { passengerAPI.fullUpdate(passenger.id!!, passenger).block() }
    }

    @Test
    fun `given that I have a registered passenger, when I request to update incrementally this passenger, then he should be updated`() {

        val passenger = randomPassenger()
        val id = passenger.id!!
        val patchPassenger = PatchPassenger(name = randomName())

        every { passengerService.findById(passenger.id!!) } returns Mono.just(passenger)
        every { passengerService.save(passenger.copy(name = patchPassenger.name!!)) } returns Mono.just(passenger.copy(name = patchPassenger.name!!))

        val updatedPassenger = passengerAPI.incrementalUpdate(id, patchPassenger).block()

        assertEquals(patchPassenger.name, updatedPassenger!!.name)
    }

    @Test
    fun `given that I have a registered passenger, when I request to delete this passenger, then he should be deleted`() {

        val passenger = randomPassenger()

        every { passengerService.findById(passenger.id!!) } returns Mono.just(passenger)
        every { passengerService.delete(passenger) } returns Mono.empty()

        passengerAPI.deleteEntity(passenger.id!!).block()
        verify { passengerService.delete(passenger) }
    }
}
