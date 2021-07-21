package com.github.alesaudate.samples.reactive.carapp.observability

import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class MetricsDataProvider(
    val metricsRegistry: MeterRegistry
) {

    fun registerServiceCall(serviceName: String, methodName: String) {
        debug("Registering service call for service {} and method {}", serviceName, methodName)
        metricsRegistry.counter("$serviceName.$methodName.calls").increment()
    }

    fun registerSuccessServiceCall(serviceName: String, methodName: String) {
        debug("Registering successful service call for service {} and method {}", serviceName, methodName)
        metricsRegistry.counter("$serviceName.$methodName.calls.successes").increment()
    }

    fun registerFailureServiceCall(serviceName: String, methodName: String) = metricsRegistry.counter("$serviceName.$methodName.calls.failures").increment()

    fun getGoogleMapsClientHitAmount() = metricsRegistry.counter("gmapsClient.getDistanceBetweenAddresses.calls").count()

    fun getGoogleMapsClientSuccessAmount() = metricsRegistry.counter("gmapsClient.getDistanceBetweenAddresses.calls.successes").count()

    fun getGoogleMapsClientFailureAmount() = metricsRegistry.counter("gmapsClient.getDistanceBetweenAddresses.calls.failures").count()
}
