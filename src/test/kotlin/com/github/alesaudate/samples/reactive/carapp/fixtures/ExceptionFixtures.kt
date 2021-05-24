package com.github.alesaudate.samples.reactive.carapp.fixtures

import io.mockk.every
import io.mockk.mockk
import org.springframework.web.bind.support.WebExchangeBindException

fun randomWebExchangeBindException(): WebExchangeBindException {
    val exception = mockk<WebExchangeBindException>(relaxed = true)
    every { exception.bindingResult.fieldErrors } returns listOf()
    return exception
}
