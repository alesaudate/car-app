package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import com.github.alesaudate.samples.reactive.carapp.extensions.info
import com.github.alesaudate.samples.reactive.carapp.interfaces.outgoing.GmapsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
class TravelService(
    val travelRequestRepository: TravelRequestRepository,
    val gmapsService: GmapsService,

    @Value("\${app.domain.travel.request.maxtime:600}")
    val maxTravelTime: Int
) {

    private val scheduler = Schedulers.boundedElastic()

    @PostConstruct
    fun init() {
        expireOldTravelRequests()
    }

    fun saveTravelRequest(travelRequest: TravelRequest): Mono<TravelRequest> {
        return Mono.fromCallable {
            travelRequestRepository.save(travelRequest)
        }.subscribeOn(scheduler)
    }

    fun findNearbyTravelRequests(currentAddress: String): Flux<TravelRequest> {

        return findCreatedTravelRequests()
            .flatMap { tr -> gmapsService.getDistanceBetweenAddresses(currentAddress, tr.origin).map { tr to it } }
            .filter { it.second <= maxTravelTime }
            .map { it.first }
    }

    @Scheduled(cron = "*/30 * * * * *")
    fun expireOldTravelRequests() {

        val reference = LocalDateTime.now().minusMinutes(10)
        debug("Running verification of expired travel requests (prior than {})", reference)
        Flux.fromIterable(travelRequestRepository.findByStatusCreatedAndCreationDateBefore(reference))
            .subscribe {
                info("About to expire travel request {}", it)
                val tr = it.copy(status = TravelRequestStatus.EXPIRED)
                travelRequestRepository.save(tr)
                info("Expired travel request {}", tr)
            }
    }

    private fun findCreatedTravelRequests(): Flux<TravelRequest> {
        return Flux.fromIterable(travelRequestRepository.findByStatus(TravelRequestStatus.CREATED))
            .cache(Duration.ofSeconds(30))
            .subscribeOn(scheduler)
    }
}
