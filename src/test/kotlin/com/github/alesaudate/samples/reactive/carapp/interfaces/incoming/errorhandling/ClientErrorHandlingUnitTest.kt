package com.github.alesaudate.samples.reactive.carapp.interfaces.incoming.errorhandling

import com.github.alesaudate.samples.reactive.carapp.randomCode
import com.github.alesaudate.samples.reactive.carapp.randomMessage
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError
import org.springframework.web.bind.support.WebExchangeBindException
import java.util.Locale

class ClientErrorHandlingUnitTest {

    lateinit var messageSource: MessageSource

    lateinit var clientErrorHandling: ClientErrorHandling

    @BeforeEach
    fun setup() {
        messageSource = mockk()
        clientErrorHandling = ClientErrorHandling(messageSource)
    }

    @Test
    fun `given a WebExchangeBindException, when I translate it to a error response, then I should get an appropriate error response`() {

        val locale = Locale.getDefault()
        val webExchangeBindException = mockk<WebExchangeBindException>(relaxed = true)
        val fieldError = mockk<FieldError>()
        val message = randomMessage()

        every { webExchangeBindException.bindingResult.fieldErrors } returns listOf(fieldError)
        every { messageSource.getMessage(fieldError, locale) } returns message

        val response = clientErrorHandling.handleMethodArgumentNotValid(webExchangeBindException, locale)

        assertEquals(1, response.errors.size)
        assertEquals(message, response.errors[0].message)
    }

    @Test
    fun `given a EntityNotFoundException, when I translate it to a error response, then I should get an appropriate error response`() {

        val locale = Locale.getDefault()
        val code = randomCode()
        val entityNotFoundException = EntityNotFoundException(code)
        val message = randomMessage()

        every { messageSource.getMessage("NotFound.$code", null, locale) } returns message

        val response = clientErrorHandling.handleEntityNotFound(entityNotFoundException, locale)

        assertEquals(1, response.errors.size)
        assertEquals(message, response.errors[0].message)
    }
}
