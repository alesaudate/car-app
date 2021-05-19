package com.github.alesaudate.samples.reactive.carapp.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface DriverRepository : JpaRepository<Driver, Long>

interface PassengerRepository : JpaRepository<Passenger, Long>

interface TravelRequestRepository : JpaRepository<TravelRequest, Long> {

    fun findByStatus(status: TravelRequestStatus): List<TravelRequest>

    @Query("select t from TravelRequest t where t.status = 'CREATED' AND t.creationDate < :creationDate")
    fun findByStatusCreatedAndCreationDateBefore(@Param("creationDate") creationDate: LocalDateTime): List<TravelRequest>
}
