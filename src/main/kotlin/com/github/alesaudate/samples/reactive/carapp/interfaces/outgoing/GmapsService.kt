package com.github.alesaudate.samples.reactive.carapp.interfaces.outgoing.gmaps

import com.github.alesaudate.samples.reactive.carapp.extensions.debug
import com.github.alesaudate.samples.reactive.carapp.extensions.warn
import com.github.alesaudate.samples.reactive.carapp.observability.Observed
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import net.minidev.json.JSONArray
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.lang.RuntimeException

@Service
class GmapsService(
    @Value("\${app.interfaces.outcoming.gmaps.appKey}")
    val appKey: String,

    @Value("\${app.interfaces.outcoming.gmaps.host:https://maps.googleapis.com}")
    val gMapsHost: String,

    @Qualifier("GMaps")
    val webClientBuilder: WebClient.Builder
) {

    @Observed("gmapsClient")
    @CircuitBreaker(name = "GMaps")
    fun getDistanceBetweenAddresses(addressOne: String, addressTwo: String): Mono<Int> {

        return webClientBuilder
            .baseUrl(gMapsHost)
            .build()
            .get()
            .uri { uriBuilder ->
                uriBuilder.path(GMAPS_RESOURCE)
                    .queryParam("origin", addressOne)
                    .queryParam("destination", addressTwo)
                    .queryParam("key", appKey)
                    .build()
            }
            .retrieve()
            .toEntity(String::class.java)
            .mapNotNull { it.body }
            .map { asDocumentContext(it!!) }
            .doOnNext { detectError(it) }
            .map { findDuration(it!!) }
            .doOnError {
                throwable ->
                warn("Google Maps could not fetch data for distance between $addressOne and $addressTwo", throwable)
            }
    }

    private fun asDocumentContext(body: String) = JsonPath.parse(body)

    private fun detectError(documentContext: DocumentContext) {

        try {
            val errorMessage = documentContext.read<String>("\$.error_message")
            errorMessage.let { throw GMapsException(it) }
        } catch (e: PathNotFoundException) {
            debug("No errors have been found in {}", documentContext)
        }
    }

    private fun findDuration(documentContext: DocumentContext): Int {

        val rawResults: JSONArray = documentContext.read("\$..legs[*].duration.value")
        return rawResults.map { it as Int }.minOrNull() ?: Int.MAX_VALUE
    }

    companion object {
        private const val GMAPS_RESOURCE = "/maps/api/directions/json"
    }
}

data class GMapsException(override val message: String?) : RuntimeException(message)
