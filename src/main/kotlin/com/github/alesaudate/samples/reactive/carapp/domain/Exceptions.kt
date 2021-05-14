package com.github.alesaudate.samples.reactive.carapp.domain

import java.lang.RuntimeException

open class BusinessException() : RuntimeException()

open class DriverNotFoundException() : BusinessException()