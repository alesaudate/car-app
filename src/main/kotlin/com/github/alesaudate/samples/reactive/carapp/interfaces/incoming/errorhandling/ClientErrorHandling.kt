package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.util.*

@RestControllerAdvice
class ClientErrorHandling(
    val messageSource: MessageSource
){

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
    fun handleEntityNotFound(ex: EntityNotFoundException, locale: Locale) = ErrorResponse(listOf(getMessage(ex.resolveCode(), locale)))


    fun getMessage(code: String, locale: Locale) = ErrorData(messageSource.getMessage(code, null, locale))

    fun getMessage(fieldError: FieldError, locale: Locale) =
        ErrorData(messageSource.getMessage(fieldError, locale))


}