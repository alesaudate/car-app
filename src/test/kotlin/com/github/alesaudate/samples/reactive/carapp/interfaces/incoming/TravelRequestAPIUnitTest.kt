package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.github.alesaudate.samples.reactive.carapp.domain.TravelService
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomTravelRequest
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomTravelRequestInput
import com.github.alesaudate.samples.reactive.carapp.fixtures.randomTravelRequestOutput
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.mapping.TravelRequestMapper
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@SpringBootTest(classes = [TravelRequestAPI::class])
class TravelRequestAPIUnitTest {

    @MockkBean
    lateinit var travelService: TravelService

    @MockkBean
    lateinit var travelRequestMapper: TravelRequestMapper

    @Autowired
    lateinit var travelRequestAPI: TravelRequestAPI

    @Test
    fun `given a travel request API, when I request to create a new travel request, then I should receive a new travel request data`() {

        val travelRequestInput = randomTravelRequestInput()
        val travelRequest = randomTravelRequest(id = null)
        val travelRequestWithId = travelRequest.copy(id = randomId())
        val travelRequestOutput = randomTravelRequestOutput()

        every { travelRequestMapper.map(travelRequestInput) } returns Mono.just(travelRequest)
        every { travelService.saveTravelRequest(travelRequest) } returns Mono.just(travelRequestWithId)
        every { travelRequestMapper.map(travelRequest = travelRequestWithId) } returns Mono.just(travelRequestOutput)
        every { travelRequestMapper.buildOutputModel(travelRequestWithId, travelRequestOutput) } answers { callOriginal() }

        val entityModel = travelRequestAPI.makeTravelRequest(travelRequestInput).block()

        assertEquals(travelRequestOutput, entityModel!!.content)
    }

    @Test
    fun `given that some travel requests are registered with nearby adresses, when I request a list of nearby travel requests, then I should get a list of nearby travel requests`() {

        val travelRequestOne = randomTravelRequest()
        val travelRequestOutputOne = randomTravelRequestOutput()
        val travelRequestTwo = randomTravelRequest()
        val travelRequestOutputTwo = randomTravelRequestOutput()

        val originAddress = randomAddress()

        every { travelService.findNearbyTravelRequests(originAddress) } returns Flux.fromIterable(mutableListOf(travelRequestOne, travelRequestTwo))
        every { travelRequestMapper.map(travelRequestOne) } returns Mono.just(travelRequestOutputOne)
        every { travelRequestMapper.map(travelRequestTwo) } returns Mono.just(travelRequestOutputTwo)
        every { travelRequestMapper.buildOutputModel(any(), any()) } answers { callOriginal() }

        val travelRequests = travelRequestAPI.listNearbyRequests(originAddress).buffer(2).blockFirst()!!

        assertTrue(travelRequests.isNotEmpty())
        assertEquals(2, travelRequests.size)
        assertEquals(travelRequestOutputOne, travelRequests[0].content!!)
        assertEquals(travelRequestOutputTwo, travelRequests[1].content!!)
    }
}
