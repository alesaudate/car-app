package com.github.alesaudate.samples.reactive.carapp.observability

import com.github.alesaudate.samples.reactive.carapp.randomDouble
import com.github.alesaudate.samples.reactive.carapp.randomServiceName
import com.ninjasquad.springmockk.MockkBean
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [MetricsDataProvider::class])
class MetricsDataProviderUnitTest {

    @Autowired
    lateinit var metricsDataProvider: MetricsDataProvider

    @MockkBean
    lateinit var meterRegistry: MeterRegistry

    @MockkBean(relaxed = true)
    lateinit var counter: Counter

    @Test
    fun `given a metrics data provider, when I register a new service call, then one more call should get registered`() {
        val serviceName = randomServiceName()
        val methodName = randomServiceName()

        every { meterRegistry.counter("$serviceName.$methodName.calls") } returns counter

        metricsDataProvider.registerServiceCall(serviceName, methodName)

        verify { counter.increment() }
    }

    @Test
    fun `given a metrics data provider, when I register a new successful service call, then one more call should get registered`() {
        val serviceName = randomServiceName()
        val methodName = randomServiceName()

        every { meterRegistry.counter("$serviceName.$methodName.calls.successes") } returns counter

        metricsDataProvider.registerSuccessServiceCall(serviceName, methodName)

        verify { counter.increment() }
    }

    @Test
    fun `given a metrics data provider, when I register a new failed service call, then one more call should get registered`() {
        val serviceName = randomServiceName()
        val methodName = randomServiceName()

        every { meterRegistry.counter("$serviceName.$methodName.calls.failures") } returns counter

        metricsDataProvider.registerFailureServiceCall(serviceName, methodName)

        verify { counter.increment() }
    }

    @Test
    fun `given a metrics data provider, when I ask for a count on calls for distance between addresses calls, then I should get the count`() {

        val count = randomDouble()

        every { meterRegistry.counter("gmapsClient.getDistanceBetweenAddresses.calls") } returns counter
        every { counter.count() } returns count

        val returnedResult = metricsDataProvider.getGoogleMapsClientHitAmount()

        assertEquals(count, returnedResult)
    }

    @Test
    fun `given a metrics data provider, when I ask for a count on successful calls for distance between addresses calls, then I should get the count`() {

        val count = randomDouble()

        every { meterRegistry.counter("gmapsClient.getDistanceBetweenAddresses.calls.successes") } returns counter
        every { counter.count() } returns count

        val returnedResult = metricsDataProvider.getGoogleMapsClientSuccessAmount()

        assertEquals(count, returnedResult)
    }

    @Test
    fun `given a metrics data provider, when I ask for a count on failed calls for distance between addresses calls, then I should get the count`() {

        val count = randomDouble()

        every { meterRegistry.counter("gmapsClient.getDistanceBetweenAddresses.calls.failures") } returns counter
        every { counter.count() } returns count

        val returnedResult = metricsDataProvider.getGoogleMapsClientFailureAmount()

        assertEquals(count, returnedResult)
    }
}
