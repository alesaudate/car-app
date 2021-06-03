package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.randomDateInThePast
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PassengerTest {

    @Test
    fun `test properties set are fetched back correctly`() {
        val id = randomId()
        val name = randomName()
        val passenger = Passenger(id, name)

        assertEquals(id, passenger.id)
        assertEquals(name, passenger.name)
    }
}