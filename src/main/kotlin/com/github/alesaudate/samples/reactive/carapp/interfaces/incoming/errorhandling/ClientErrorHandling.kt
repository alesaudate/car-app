package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling

import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.util.Locale

@RestControllerAdvice
class ClientErrorHandling(
    val messageSource: MessageSource
) {

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValid(ex: WebExchangeBindException, locale: Locale): ErrorResponse {
        val messages = ex.bindingResult.fieldErrors.map {
            getMessage(it, locale)
        }
        return ErrorResponse(messages)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleEntityNotFound(ex: EntityNotFoundException, locale: Locale): ErrorResponse {
        debug("Entity has not been found", ex)
        return ErrorResponse(listOf(getMessage(ex.resolveCode(), locale)))
    }

    private fun getMessage(code: String, locale: Locale) = ErrorData(messageSource.getMessage(code, null, locale))

    private fun getMessage(fieldError: FieldError, locale: Locale) =
        ErrorData(messageSource.getMessage(fieldError, locale))
}
