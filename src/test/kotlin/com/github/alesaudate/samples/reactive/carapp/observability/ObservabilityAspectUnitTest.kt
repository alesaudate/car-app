package com.github.alesaudate.samples.reactive.carapp.observability

import com.github.alesaudate.samples.reactive.carapp.config.AspectsConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component

@SpringBootTest(classes = [AspectsConfig::class, ObservabilityAspect::class, UsedForTesting::class])
internal class ObservabilityAspectUnitTest {

    @Autowired
    lateinit var usedForTesting: UsedForTesting

    @MockkBean(relaxed = true)
    lateinit var metricsRegistry: MetricsDataProvider

    @Test
    fun `given an observability aspect, when I call an intercepted method And the method returns OK, then the aspect should register for a success response`() {

        usedForTesting.functionThatAlwaysReturnsOK()
        verify(exactly = 1) { metricsRegistry.registerServiceCall("testing", "functionThatAlwaysReturnsOK") }
        verify(exactly = 1) { metricsRegistry.registerSuccessServiceCall("testing", "functionThatAlwaysReturnsOK") }
        verify(exactly = 0) { metricsRegistry.registerFailureServiceCall("testing", "functionThatAlwaysReturnsOK") }
    }

    @Test
    fun `given an observability aspect, when I call an intercepted method And the method throws exception, then the aspect should register for a failure response`() {

        assertThrows<RuntimeException> { usedForTesting.functionThatAlwaysThrowException() }
        verify(exactly = 1) { metricsRegistry.registerServiceCall("testing", "functionThatAlwaysThrowException") }
        verify(exactly = 0) { metricsRegistry.registerSuccessServiceCall("testing", "functionThatAlwaysThrowException") }
        verify(exactly = 1) { metricsRegistry.registerFailureServiceCall("testing", "functionThatAlwaysThrowException") }
    }
}

@Component
class UsedForTesting {

    @Observed("testing")
    fun functionThatAlwaysReturnsOK() = "blablaba"

    @Observed("testing")
    fun functionThatAlwaysThrowException(): String = throw RuntimeException()
}
