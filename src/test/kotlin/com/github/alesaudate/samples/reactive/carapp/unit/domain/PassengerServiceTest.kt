package com.github.alesaudate.samples.reactive.carapp.unit.domain

import com.github.alesaudate.samples.reactive.carapp.domain.PassengerRepository
import com.github.alesaudate.samples.reactive.carapp.domain.PassengerService
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomPassenger
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull


@SpringBootTest(classes = [PassengerService::class])
class PassengerServiceTest {


    @Autowired
    lateinit var passengerService: PassengerService


    @MockkBean
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

}