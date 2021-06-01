package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming

import com.github.alesaudate.samples.reactive.carapp.extensions.ISO_LOCAL_DATE_PATTERN
import com.github.alesaudate.samples.reactive.carapp.extensions.TestContainersExtension
import com.github.alesaudate.samples.reactive.carapp.extensions.loadFileContents
import com.github.alesaudate.samples.reactive.carapp.extensions.toIsoDate
import com.github.alesaudate.samples.reactive.carapp.randomDateInThePast
import com.github.alesaudate.samples.reactive.carapp.randomId
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@ExtendWith(TestContainersExtension::class)
class DriverAPIIT {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost:$port"
    }

    @Test
    fun `test create new driver`() {
        createDriver(randomName(), randomDateInThePast())
    }

    @Test
    fun `test find driver`() {
        val driverName = randomName()
        val birthDate = randomDateInThePast()
        val id = createDriver(driverName, birthDate)

        given()
            .contentType(ContentType.JSON)
            .get("/drivers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo(driverName))
            .body("birth_date", equalTo(formatDate(birthDate)))
    }

    @Test
    fun `test find driver that does not exist`() {

        val id = randomId()

        given()
            .contentType(ContentType.JSON)
            .get("/drivers/$id")
            .then()
            .statusCode(404)
            .body("errors[0].message", equalTo("Driver has not been found"))
    }

    @Test
    fun `test find driver that does not exist with locale pt-BR`() {

        val id = randomId()

        given()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "pt-BR")
            .get("/drivers/$id")
            .then()
            .statusCode(404)
            .body("errors[0].message", equalTo("Motorista não encontrado"))
    }

    @Test
    fun `test update driver (full)`() {
        val initialDriverName = randomName()
        val updatedDriverName = randomName()

        val initialBirthDate = randomDateInThePast()
        val updatedBirthDate = randomDateInThePast()

        val id = createDriver(initialDriverName, initialBirthDate)

        given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/drivers/create_driver.json", mapOf("name" to updatedDriverName, "birthDate" to formatDate(updatedBirthDate))))
            .put("/drivers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo(updatedDriverName))
            .body("birth_date", equalTo(formatDate(updatedBirthDate)))
    }

    @Test
    fun `test update driver name through patch`() {
        val initialDriverName = randomName()
        val updatedDriverName = randomName()

        val id = createDriver(initialDriverName, randomDateInThePast())

        given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/drivers/update_driver_name.json", mapOf("name" to updatedDriverName)))
            .patch("/drivers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo(updatedDriverName))
            .body("birth_date", notNullValue())
    }

    @Test
    fun `test update driver birth date through patch`() {
        val initialBirthDate = randomDateInThePast()
        val updatedBirthDate = randomDateInThePast().toIsoDate()

        val id = createDriver(randomName(), initialBirthDate)

        given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/drivers/update_driver_birth_date.json", mapOf("birthDate" to updatedBirthDate)))
            .patch("/drivers/$id")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("birth_date", equalTo(updatedBirthDate))
            .body("name", notNullValue())
    }

    @Test
    fun `test delete driver`() {

        val id = createDriver(randomName(), randomDateInThePast())

        given()
            .delete("/drivers/$id")
            .then()
            .statusCode(200)

        given()
            .contentType(ContentType.JSON)
            .get("/drivers/$id")
            .then()
            .statusCode(404)
    }

    fun createDriver(driverName: String, birthDate: LocalDate): Long {

        val birthDateToString = formatDate(birthDate)

        return given()
            .contentType(ContentType.JSON)
            .body(loadFileContents("/requests/drivers/create_driver.json", mapOf("name" to driverName, "birthDate" to birthDateToString)))
            .post("/drivers")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .extract()
            .body()
            .jsonPath().getLong("id")
    }

    fun formatDate(date: LocalDate) = DateTimeFormatter.ofPattern(ISO_LOCAL_DATE_PATTERN).run { format(date) }
}
