package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.admin

import com.github.alesaudate.samples.reactive.carapp.observability.MetricsDataProvider
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Service
@RestController
@RequestMapping("/admin/metrics")
class MetricsAPI(
    val metricsDataProvider: MetricsDataProvider
) {



    @GetMapping("/google/maps/distance/count/hits")
    fun googleMapsCalls() = metricsDataProvider.getGoogleMapsClientHitAmount()

    @GetMapping("/google/maps/distance/count/successes")
    fun googleMapsCallsSuccesses() = metricsDataProvider.getGoogleMapsClientSuccessAmount()

    @GetMapping("/google/maps/distance/count/failures")
    fun googleMapsCallsFailures() = metricsDataProvider.getGoogleMapsClientFailureAmount()


}