package com.github.alesaudate.samples.reactive.carapp.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.alesaudate.samples.reactive.carapp.extensions.ISO_LOCAL_DATE_PATTERN
import org.springframework.data.annotation.Immutable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Size

@Entity
@Immutable
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Driver(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @get:Size(min = 5, max = 255)
    val name: String,

    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_LOCAL_DATE_PATTERN)
    val birthDate: LocalDate
)

@Entity
@Immutable
data class Passenger(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String
)

@Entity
@Immutable
data class TravelRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,


    @ManyToOne
    val passenger: Passenger,
    val origin: String,
    val destination: String,

    @Enumerated(EnumType.STRING)
    val status: TravelRequestStatus = TravelRequestStatus.CREATED,
    val creationDate: LocalDateTime = LocalDateTime.now()

)

enum class TravelRequestStatus {
    CREATED, EXPIRED, ACCEPTED, COMPLETED
}