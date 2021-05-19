package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.fixtures.randomDriver
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

@SpringBootTest(classes = [DriverService::class])
class DriverServiceUnitTest {

    @Autowired
    lateinit var driverService: DriverService

    @MockkBean(relaxed = true)
    lateinit var driverRepository: DriverRepository

    @Test
    fun `given an existing driver ID, when I search for a driver with this ID, then I should get the driver`() {

        val id = randomId()
        val driver = randomDriver(id = id)
        every { driverRepository.findByIdOrNull(id) } returns driver

        val foundDriver = driverService.findById(id).block()
        assertEquals(driver, foundDriver)
    }

    @Test
    fun `given a non-existent driver ID, when I search for a driver with this ID, then I should get an empty result`() {

        val id = randomId()
        every { driverRepository.findByIdOrNull(id) } returns null

        val foundDriver = driverService.findById(id).block()
        assertNull(foundDriver)
    }

    @Test
    fun `given that there are several drivers on the repository, when I request these records, then I should get a Flux with them`() {

        val listOfData = listOfData(randomPositiveInt(until = 100)) { randomDriver() }
        every { driverRepository.findAll(any<Pageable>()) } returns PageImpl(listOfData, Pageable.unpaged(), listOfData.size.toLong())

        StepVerifier.create(driverService.findAll()).expectNextCount(listOfData.size.toLong()).verifyComplete()
    }

    @Test
    fun `given some unsaved driver, when I request to save it, then I should get a Mono with the saved driver`() {

        val driver = randomDriver(id = null)
        val id = randomId()

        every { driverRepository.save(driver) } returns driver.copy(id = id)

        val returnedDriver = driverService.save(driver).block()
        assertEquals(driver.copy(id = id), returnedDriver)
    }

    @Test
    fun `given a driver, when I request to delete it, then it should be deleted`() {

        val driver = randomDriver()

        driverService.delete(driver).block()

        verify { driverRepository.delete(driver) }
    }
}
