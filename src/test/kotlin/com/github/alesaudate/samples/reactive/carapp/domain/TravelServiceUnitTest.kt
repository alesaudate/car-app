package com.github.alesaudate.samples.reactive.carapp.domain

import com.github.alesaudate.samples.reactive.carapp.fixtures.randomTravelRequest
import com.github.alesaudate.samples.reactive.carapp.interfaces.outgoing.GmapsService
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

@SpringBootTest(classes = [TravelService::class])
class TravelServiceUnitTest {

    @Autowired
    lateinit var travelService: TravelService

    @MockkBean(relaxed = true)
    lateinit var travelRequestRepository: TravelRequestRepository

    @MockkBean
    lateinit var gmapsService: GmapsService

    @Test
    fun `given a travel request, when I save a travel request, then I should get a saved travel request`() {

        val travelRequest = randomTravelRequest(id = null)
        val id = randomId()

        every { travelRequestRepository.save(travelRequest) } returns travelRequest.copy(id = id)

        val responseTravelRequest = travelService.saveTravelRequest(travelRequest).block()

        assertEquals(travelRequest.copy(id = id), responseTravelRequest)
    }

    @Test
    fun `given an address And a travel request with a close address, when I request a list of nearby travel requests, then I should get a travel request with a close address`() {

        val address = randomAddress()
        val travelRequest = randomTravelRequest()
        val randomTravelTime = Random.nextInt(600)

        every { travelRequestRepository.findByStatus(TravelRequestStatus.CREATED) } returns listOf(travelRequest)
        every { gmapsService.getDistanceBetweenAddresses(address, travelRequest.origin) } returns Mono.just(randomTravelTime)

        val foundTravelRequest = travelService.findNearbyTravelRequests(address).blockFirst()

        assertEquals(travelRequest, foundTravelRequest)
    }

    @Test
    fun `given an address And a travel request with a far address, when I request a list of nearby travel requests, then I should get an empty list`() {

        val address = randomAddress()
        val travelRequest = randomTravelRequest()
        val travelTime = 601

        every { travelRequestRepository.findByStatus(TravelRequestStatus.CREATED) } returns listOf(travelRequest)
        every { gmapsService.getDistanceBetweenAddresses(address, travelRequest.origin) } returns Mono.just(travelTime)

        val foundTravelRequest = travelService.findNearbyTravelRequests(address).blockFirst()

        assertNull(foundTravelRequest)
    }

    @Test
    fun `given a travel request created more than 10 minutes ago, when I request to expire old travel requests, then the travel request has its status updated to EXPIRED`() {

        val travelRequest = randomTravelRequest(status = TravelRequestStatus.CREATED, creationDate = LocalDateTime.now().minusMinutes(10))
        val listOfSavedTravelRequests = mutableListOf<TravelRequest>()

        every { travelRequestRepository.findByStatusCreatedAndCreationDateBefore(any()) } returns listOf(travelRequest)
        every { travelRequestRepository.save(capture(listOfSavedTravelRequests)) } returnsArgument (0)

        travelService.expireOldTravelRequests()

        await.atMost(Duration.ofSeconds(1)).until { listOfSavedTravelRequests.isNotEmpty() }

        val savedTravelRequest = listOfSavedTravelRequests[0]

        assertEquals(travelRequest.copy(status = TravelRequestStatus.EXPIRED), savedTravelRequest)
    }
}
