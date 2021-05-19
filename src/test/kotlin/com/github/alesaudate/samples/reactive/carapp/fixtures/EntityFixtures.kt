package com.github.alesaudate.samples.reactive.carapp.fixtures

import com.github.alesaudate.samples.reactive.carapp.domain.Driver
import com.github.alesaudate.samples.reactive.carapp.domain.Passenger
import com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.PatchDriver
import com.github.alesaudate.samples.reactive.carapp.randomDateInThePast
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.alesaudate.samples.reactive.carapp.randomName
import java.time.LocalDate

fun randomPassenger(id: Long? = randomId(), name: String = randomName()) =
    Passenger(id = id, name = name)

fun randomDriver(id: Long? = randomId(), name: String = randomName(), birthDate: LocalDate = randomDateInThePast()) =
    Driver(id = id, name = name, birthDate = birthDate)

fun randomPatchDriver(name: String? = randomName(), birthDate: LocalDate? = randomDateInThePast()) = PatchDriver(name = name, birthDate = birthDate)
