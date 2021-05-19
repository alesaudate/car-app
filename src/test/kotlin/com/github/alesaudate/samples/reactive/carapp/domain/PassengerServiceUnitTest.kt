package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.fixtures.randomPassenger
import com.github.alesaudate.samples.reactive.carapp.listOfData
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomPositiveInt
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import reactor.test.StepVerifier

@SpringBootTest(classes = [PassengerService::class])
class PassengerServiceUnitTest {

    @Autowired
    lateinit var passengerService: PassengerService

    @MockkBean(relaxed = true)
    lateinit var passengerRepository: PassengerRepository

    @Test
    fun `given an existing passenger ID, when I search for a passenger with this ID, then I should get the passenger`() {

        val id = randomId()
        val passenger = randomPassenger(id = id)
        every { passengerRepository.findByIdOrNull(id) } returns passenger

        val foundPassenger = passengerService.findById(id).block()
        assertEquals(passenger, foundPassenger)
    }

    @Test
    fun `given a non-existent passenger ID, when I search for a passenger with this ID, then I should get an empty result`() {

        val id = randomId()
        every { passengerRepository.findByIdOrNull(id) } returns null

        val foundPassenger = passengerService.findById(id).block()
        assertNull(foundPassenger)
    }

    @Test
    fun `given that there are several passengers on the repository, when I request these records, then I should get a Flux with them`() {

        val listOfData = listOfData(randomPositiveInt(until = 100)) { randomPassenger() }
        every { passengerRepository.findAll(any<Pageable>()) } returns PageImpl(listOfData, Pageable.unpaged(), listOfData.size.toLong())

        StepVerifier.create(passengerService.findAll()).expectNextCount(listOfData.size.toLong()).verifyComplete()
    }

    @Test
    fun `given some unsaved passenger, when I request to save it, then I should get a Mono with the saved passenger`() {

        val passenger = randomPassenger(id = null)
        val id = randomId()

        every { passengerRepository.save(passenger) } returns passenger.copy(id = id)

        val returnedPassenger = passengerService.save(passenger).block()
        assertEquals(passenger.copy(id = id), returnedPassenger)
    }

    @Test
    fun `given a passenger, when I request to delete it, then it should be deleted`() {

        val passenger = randomPassenger()

        passengerService.delete(passenger).block()

        verify { passengerRepository.delete(passenger) }
    }
}
