package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling

import java.lang.RuntimeException

abstract class ClientException() : RuntimeException()

data class EntityNotFoundException(private val code: String) : ClientException() {
    fun resolveCode() = "NotFound.$code"
}
