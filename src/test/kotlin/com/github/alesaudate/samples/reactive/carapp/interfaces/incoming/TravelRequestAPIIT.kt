package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.github.alesaudate.samples.reactive.carapp.domain.TravelRequestStatus
import com.github.alesaudate.samples.reactive.carapp.extensions.TestContainersExtension
import com.github.alesaudate.samples.reactive.carapp.extensions.loadFileContents
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import com.github.alesaudate.samples.reactive.carapp.randomName
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ExtendWith(TestContainersExtension::class)
@AutoConfigureWireMock(port = DYNAMIC_PORT)
class TravelRequestAPIIT {

    @Autowired
    private lateinit var wiremock: WireMockServer

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost:$port"
    }

    @Test
    fun `test create travel request`() {

        val passengerId = createPassenger()
        val origin = randomAddress()
        val destination = randomAddress()

        createTravelRequest(passengerId, origin, destination)
    }

    @Test
    @DirtiesContext
    fun `test find close travel requests`() {

        val travelOrigin = randomAddress()
        val travelDestination = randomAddress()
        val driverCurrentLocation = randomAddress()
        val passengerId = createPassenger()

        createResponseForOriginAndDestination(driverCurrentLocation, travelOrigin)

        val travelRequestId = createTravelRequest(passengerId, travelOrigin, travelDestination)

        given()
            .contentType(ContentType.JSON)
            .get("travel-requests/nearby?currentAddress=$driverCurrentLocation")
            .then()
            .statusCode(200)
            .body("id[0]", CoreMatchers.equalTo(travelRequestId.toInt()))
    }

    private fun createResponseForOriginAndDestination(origin: String, destination: String) {
        wiremock.stubFor(
            get(urlPathEqualTo("/maps/api/directions/json"))
                .withQueryParam("origin", equalTo(origin))
                .withQueryParam("destination", equalTo(destination))
                .withQueryParam("key", equalTo("123"))
                .willReturn(okJson(loadFileContents("responses/gmaps/ok_response_5_minutes.json")))
        )
    }

    private fun createTravelRequest(passengerId: Long, origin: String, destination: String): Long {
        return given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("requests/travel_requests/create_travel_request.json", mapOf("passengerId" to passengerId.toString(), "origin" to origin, "destination" to destination)))
            .post("travel-requests")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("origin", CoreMatchers.equalTo(origin))
            .body("destination", CoreMatchers.equalTo(destination))
            .body("status", CoreMatchers.equalTo(TravelRequestStatus.CREATED.name))
            .body("creation_date", notNullValue())
            .extract()
            .body()
            .jsonPath()
            .getLong("id")
    }

    private fun createPassenger(passengerName: String = randomName()): Long {
        return given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/passengers/create_passenger.json", mapOf("name" to passengerName)))
            .post("/passengers")
            .then()
            .statusCode(200)
            .body("id", CoreMatchers.notNullValue())
            .extract()
            .body()
            .jsonPath().getLong("id")
    }
}
