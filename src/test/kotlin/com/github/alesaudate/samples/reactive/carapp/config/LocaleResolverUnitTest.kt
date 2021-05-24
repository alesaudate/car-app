package com.github.alesaudate.samples.reactive.carapp.config

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.server.ServerWebExchange
import java.util.Locale

class LocaleResolverUnitTest {

    lateinit var localeResolver: LocaleResolver

    @BeforeEach
    fun setup() {
        localeResolver = LocaleResolver(Locale.ENGLISH)
    }

    @Test
    fun `given a request with a null Accept-Language header, when I try to evaluate the locale, then I should get the default locale as response`() {
        val header: String? = null
        val locale = executeRequest(header, localeResolver)
        assertEquals(Locale.ENGLISH, locale)
    }

    @Test
    fun `given a request with an empty-list Accept-Language header, when I try to evaluate the locale, then I should get the default locale as response`() {
        val header = emptyList<String>()
        val locale = executeRequest(header, localeResolver)
        assertEquals(Locale.ENGLISH, locale)
    }

    @Test
    fun `given a request with a list with a null value as Accept-Language header, when I try to evaluate the locale, then I should get the default locale as response`() {
        val header = listOf<String?>(null)
        val locale = executeRequest(header, localeResolver)
        assertEquals(Locale.ENGLISH, locale)
    }

    @Test
    fun `given a request with a blank Accept-Language header, when I try to evaluate the locale, then I should get the default locale as response`() {
        val locale = executeRequest("   ", localeResolver)
        assertEquals(Locale.ENGLISH, locale)
    }

    @Test
    fun `given a request with Accept-Language header with non-acceptable value, when I try to evaluate the locale, then I should get the default locale as response`() {
        val locale = executeRequest("cn", localeResolver)
        assertEquals(Locale.ENGLISH, locale)
    }

    @Test
    fun `given a request with wildcard Accept-Language header, when I try to evaluate the locale, then I should get the default locale as response`() {
        val locale = executeRequest("*", localeResolver)
        assertEquals(Locale.ENGLISH, locale)
    }

    @Test
    fun `given a request with Accept-Language with other than the default locale, when I try to evaluate the locale, then I should get the alternative locale as response`() {
        val locale = executeRequest("pt-BR", localeResolver)
        assertEquals(Locale("pt", "BR"), locale)
    }

    private fun executeRequest(acceptLanguageHeader: String?, localeResolver: LocaleResolver) = executeRequest(
        acceptLanguageHeader?.let { listOf(it) },
        localeResolver
    )

    private fun executeRequest(acceptLanguageHeader: List<String?>?, localeResolver: LocaleResolver): Locale {
        val exchange = mockk<ServerWebExchange>(relaxed = true)
        every { exchange.request.headers["Accept-Language"] } returns acceptLanguageHeader

        return localeResolver.resolveLocaleContext(exchange).locale!!
    }
}
