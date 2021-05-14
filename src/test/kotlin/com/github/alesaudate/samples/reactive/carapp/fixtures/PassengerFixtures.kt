package com.github.alesaudate.samples.reactive.carapp.fixtures

import com.github.alesaudate.samples.reactive.carapp.domain.Passenger
import com.github.alesaudate.samples.reactive.carapp.randomId
import com.github.javafaker.Faker

private val FAKER = Faker()

fun randomPassenger(id: Long? = randomId(), name:String = FAKER.name().fullName()) =
    Passenger(id = id, name = name)