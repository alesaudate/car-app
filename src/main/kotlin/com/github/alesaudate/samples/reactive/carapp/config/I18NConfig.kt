package com.github.alesaudate.samples.reactive.carapp.config

import org.apache.commons.lang3.LocaleUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContext
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver
import java.util.Locale

@Configuration
class I18NConfig(
    @Value("\${app.i18n.locale.default:en}") val defaultLocale: String
) : DelegatingWebFluxConfiguration() {

    @Bean
    fun messageSource() = ReloadableResourceBundleMessageSource().apply {
        setBasename("classpath:/i18n/messages")
        setDefaultEncoding("UTF-8")
    }

    override fun createLocaleContextResolver() = LocaleResolver(LocaleUtils.toLocale(defaultLocale))
}

class LocaleResolver(
    val defaultLocaleToResolve: Locale
) : AcceptHeaderLocaleContextResolver() {

    companion object {
        private val ACCEPTED_LOCALES = listOf(Locale("en"), Locale("pt", "BR"))
    }

    override fun resolveLocaleContext(exchange: ServerWebExchange): LocaleContext {
        val acceptLanguageHeader = exchange.request.headers["Accept-Language"]?.firstOrNull()
        if (acceptLanguageHeader.isNullOrEmpty() || acceptLanguageHeader.trim() == "*") {
            return LocaleContext { defaultLocaleToResolve }
        }
        val list = Locale.LanguageRange.parse(acceptLanguageHeader)
        val foundLocale = Locale.lookup(list, ACCEPTED_LOCALES.plus(defaultLocaleToResolve))
        return foundLocale?.let {
            LocaleContext { it }
        } ?: LocaleContext { defaultLocaleToResolve }
    }
}
