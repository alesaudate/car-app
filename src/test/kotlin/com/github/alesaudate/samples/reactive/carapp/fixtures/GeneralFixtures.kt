package com.github.alesaudate.samples.reactive.carapp

import com.github.javafaker.Faker
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.Random.Default.nextLong
import kotlin.reflect.KClass

private val FAKER = Faker()

fun randomId() = nextLong(0, Long.MAX_VALUE)

fun randomAddress() = FAKER.address().streetAddressNumber()

fun randomPositiveInt(from: Int = 0, until: Int = Int.MAX_VALUE) = Random.nextInt(from, until)

fun randomDateInThePast(years: Int = 50) = FAKER.date().past(365 * years, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun randomName() = FAKER.name().fullName()

inline fun <T : Any> listOfData(size: Int, lmbd: () -> T): List<T> {

    val dataList = mutableListOf<T>()
    for (i in 1..size) dataList.add(lmbd())
    return dataList.toList()
}

fun <T : Enum<*>> KClass<T>.random(): T {
    val randomIndex = Random.nextInt(this.java.enumConstants.size)
    return this.java.enumConstants[randomIndex]
}
