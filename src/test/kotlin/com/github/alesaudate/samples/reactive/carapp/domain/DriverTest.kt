package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.randomDateInThePast
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DriverTest {

    @Test
    fun `test properties set are fetched back correctly`() {
        val id = randomId()
        val name = randomName()
        val birthDate = randomDateInThePast()
        val driver = Driver(id, name, birthDate)

        assertEquals(id, driver.id)
        assertEquals(name, driver.name)
        assertEquals(birthDate, driver.birthDate)
    }
}
