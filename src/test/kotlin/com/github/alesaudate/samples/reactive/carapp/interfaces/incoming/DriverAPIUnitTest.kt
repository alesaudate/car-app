package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.github.alesaudate.samples.reactive.carapp.domain.DriverService
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomDriver
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling.EntityNotFoundException
import com.github.alesaudate.samples.reactive.carapp.listOfData
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomName
import com.github.alesaudate.samples.reactive.carapp.randomPositiveInt
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest(classes = [DriverAPI::class])
class DriverAPIUnitTest {

    @MockkBean
    lateinit var driverService: DriverService

    @Autowired
    lateinit var driverAPI: DriverAPI

    @Test
    fun `given a driver API, when I request a list of all drivers, then I should get the list`() {
        val listOfDrivers = listOfData(randomPositiveInt(until = 100)) { randomDriver() }
        every { driverService.findAll(any()) } returns Flux.fromIterable(listOfDrivers)
        val fluxOfData = driverAPI.listAll()
        StepVerifier.create(fluxOfData).expectNextCount(listOfDrivers.size.toLong()).verifyComplete()
    }

    @Test
    fun `given that I have a registered driver, when I request this driver using his ID, then I should retrieve him successfully`() {
        val driver = randomDriver()
        every { driverService.findById(driver.id!!) } returns Mono.just(driver)
        val foundDriver = driverAPI.findSingle(driver.id!!).block()
        assertEquals(driver, foundDriver)
    }

    @Test
    fun `given that I do not have a registered driver, when I request a driver using some ID, then I should get an exception`() {

        val id = randomId()
        every { driverService.findById(id) } returns Mono.empty()
        assertThrows<EntityNotFoundException> { driverAPI.findSingle(id).block() }
    }

    @Test
    fun `given a driver API, when I request to create a driver, then it is created`() {

        val driver = randomDriver(id = null)
        val id = randomId()
        every { driverService.save(driver) } returns Mono.just(driver.copy(id = id))

        val returnedDriver = driverAPI.create(driver).block()
        assertEquals(driver.copy(id = id), returnedDriver)
    }

    @Test
    fun `given that I have a registered driver, when I request to fully update using this driver name, then he should be updated`() {

        val driver = randomDriver()
        val updateDriver = driver.copy(id = null, name = randomName())
        val id = driver.id!!

        every { driverService.findById(id) } returns Mono.just(driver)
        every { driverService.save(any()) } answers { Mono.just(firstArg()) }
        val updatedDriver = driverAPI.fullUpdate(id, updateDriver).block()
        assertEquals(updateDriver.copy(id = id), updatedDriver)
    }

    @Test
    fun `given that I do not have any registered drivers, when I request to update some driver, then I should get an exception`() {

        val driver = randomDriver()
        every { driverService.findById(driver.id!!) } returns Mono.empty()

        assertThrows<EntityNotFoundException> { driverAPI.fullUpdate(driver.id!!, driver).block() }
    }

    @Test
    fun `given that I have a registered driver, when I request to update incrementally this driver, then he should be updated`() {

        val driver = randomDriver()
        val id = driver.id!!
        val patchDriver = PatchDriver(name = randomName(), birthDate = null)

        every { driverService.findById(driver.id!!) } returns Mono.just(driver)
        every { driverService.save(driver.copy(name = patchDriver.name!!)) } returns Mono.just(driver.copy(name = patchDriver.name!!))

        val updatedDriver = driverAPI.incrementalUpdate(id, patchDriver).block()

        assertEquals(driver.birthDate, updatedDriver!!.birthDate)
        assertEquals(patchDriver.name, updatedDriver.name)
    }

    @Test
    fun `given that I have a registered driver, when I request to delete this driver, then he should be deleted`() {

        val driver = randomDriver()

        every { driverService.findById(driver.id!!) } returns Mono.just(driver)
        every { driverService.delete(driver) } returns Mono.empty()

        driverAPI.deleteEntity(driver.id!!).block()
        verify { driverService.delete(driver) }
    }
}
