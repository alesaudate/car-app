package com.github.alesaudate.samples.reactive.carapp.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val ISO_LOCAL_DATE_PATTERN = "yyyy-MM-dd"

const val ISO_LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

fun LocalDate.toIsoDate() = this.format(DateTimeFormatter.ofPattern(ISO_LOCAL_DATE_PATTERN))
