package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling

data class ErrorData(
    val message: String
)

data class ErrorResponse(
    val errors: List<ErrorData>
)
