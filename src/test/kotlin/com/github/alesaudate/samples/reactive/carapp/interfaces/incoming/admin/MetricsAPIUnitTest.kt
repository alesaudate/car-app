package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.admin

import com.github.alesaudate.samples.reactive.carapp.observability.MetricsDataProvider
import com.github.alesaudate.samples.reactive.carapp.randomDouble
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [MetricsAPI::class])
class MetricsAPIUnitTest {

    @MockkBean
    lateinit var metricsDataProvider: MetricsDataProvider

    @Autowired
    lateinit var metricsAPI: MetricsAPI

    @Test
    fun `call to google maps hit counts must return correctly`() {

        val data = randomDouble()
        every { metricsDataProvider.getGoogleMapsClientHitAmount() } returns data

        val countResponse = metricsAPI.googleMapsCalls()
        assertEquals(data, countResponse.value)
    }

    @Test
    fun `call to google maps success calls must return correctly`() {

        val data = randomDouble()
        every { metricsDataProvider.getGoogleMapsClientSuccessAmount() } returns data

        val countResponse = metricsAPI.googleMapsCallsSuccesses()
        assertEquals(data, countResponse.value)
    }

    @Test
    fun `call to google maps failure calls must return correctly`() {

        val data = randomDouble()
        every { metricsDataProvider.getGoogleMapsClientFailureAmount() } returns data

        val countResponse = metricsAPI.googleMapsCallsFailures()
        assertEquals(data, countResponse.value)
    }
}