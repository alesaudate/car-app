package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.admin

import com.github.alesaudate.samples.reactive.carapp.extensions.TestContainersExtension
import com.github.alesaudate.samples.reactive.carapp.extensions.loadFileContents
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import com.github.alesaudate.samples.reactive.carapp.randomName
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ExtendWith(TestContainersExtension::class)
@AutoConfigureWireMock(port = Options.DYNAMIC_PORT)
class MetricsAPIIT {

    @Autowired
    private lateinit var wiremock: WireMockServer

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost:$port"
    }

    private fun createResponseForOriginAndDestination(origin: String, destination: String) {
        wiremock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/maps/api/directions/json"))
                .withQueryParam("origin", WireMock.equalTo(origin))
                .withQueryParam("destination", WireMock.equalTo(destination))
                .withQueryParam("key", WireMock.equalTo("123"))
                .willReturn(WireMock.okJson(loadFileContents("responses/gmaps/ok_response_5_minutes.json")))
        )
    }

    @Test
    fun `given a service that register metrics for calls, when I request succesfully a list of nearby travels, then I should be able to retrieve success metrics`() {

        val driversOrigin = randomAddress()
        val travelOrigin = randomAddress()
        val travelDestination = randomAddress()

        createResponseForOriginAndDestination(driversOrigin, travelOrigin)
        createTravelRequest(travelOrigin, travelDestination)
        requestNearbyTravelRequests(driversOrigin)

        val hitsCount = given()
            .contentType(ContentType.JSON)
            .get("admin/metrics/google/maps/distance/count/hits")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getDouble("value")


        val successCount = given()
            .contentType(ContentType.JSON)
            .get("admin/metrics/google/maps/distance/count/successes")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getDouble("value")

        assertEquals(1.0, hitsCount)
        assertEquals(1.0, successCount)

    }



    private fun requestNearbyTravelRequests(address: String) {
        given()
            .contentType(ContentType.JSON)
            .get("travel-requests/nearby?currentAddress=$address")
            .then()
            .statusCode(200)
    }


    private fun createTravelRequest(origin: String = randomAddress(), destination: String = randomAddress(), passengerId: Long = createPassenger()): Long {
        return given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("requests/travel_requests/create_travel_request.json", mapOf("passengerId" to passengerId.toString(), "origin" to origin, "destination" to destination)))
            .post("travel-requests")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getLong("id")
    }


    private fun createPassenger(passengerName: String = randomName()): Long {
        return RestAssured.given()
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