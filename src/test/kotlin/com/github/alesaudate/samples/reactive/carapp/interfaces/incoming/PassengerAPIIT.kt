package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.github.alesaudate.samples.reactive.carapp.extensions.TestContainersExtension
import com.github.alesaudate.samples.reactive.carapp.extensions.loadFileContents
import com.github.alesaudate.samples.reactive.carapp.randomName
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ExtendWith(TestContainersExtension::class)
class PassengerAPIIT {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost:$port"
    }

    @Test
    fun `test create new passenger`() {
        createPassenger(randomName())
    }

    @Test
    fun `test find passenger`() {
        val passengerName = randomName()
        val id = createPassenger(passengerName)

        given()
            .contentType(ContentType.JSON)
            .get("/passengers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo(passengerName))
    }

    @Test
    fun `test update passenger (full)`() {
        val initialPassengerName = randomName()
        val updatedPassengerName = randomName()
        val id = createPassenger(initialPassengerName)

        given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/passengers/create_passenger.json", mapOf("name" to updatedPassengerName)))
            .put("/passengers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo(updatedPassengerName))
    }

    @Test
    fun `test update passenger (patch)`() {
        val initialPassengerName = randomName()
        val updatedPassengerName = randomName()
        val id = createPassenger(initialPassengerName)

        given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/passengers/create_passenger.json", mapOf("name" to updatedPassengerName)))
            .patch("/passengers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo(updatedPassengerName))
    }

    @Test
    fun `test delete passenger`() {
        val passengerName = randomName()
        val id = createPassenger(passengerName)

        given()
            .delete("/passengers/$id")
            .then()
            .statusCode(200)

        given()
            .contentType(ContentType.JSON)
            .get("/passengers/$id")
            .then()
            .statusCode(404)
    }

    fun createPassenger(passengerName: String): Long {

        return given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/passengers/create_passenger.json", mapOf("name" to passengerName)))
            .post("/passengers")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .extract()
            .body()
            .jsonPath().getLong("id")
    }
}
