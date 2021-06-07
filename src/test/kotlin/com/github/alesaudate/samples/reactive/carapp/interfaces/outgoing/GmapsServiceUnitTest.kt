package com.github.alesaudate.samples.reactive.carapp.interfaces.outgoing

import com.github.alesaudate.samples.reactive.carapp.interfaces.outgoing.gmaps.GMapsException
import com.github.alesaudate.samples.reactive.carapp.interfaces.outgoing.gmaps.GmapsService
import com.github.alesaudate.samples.reactive.carapp.randomAddress
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriBuilderFactory
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.util.function.Consumer
import java.util.function.Function

@SpringBootTest(classes = [GmapsService::class, MockWebClientBuilder::class])
@TestPropertySource(properties = ["app.interfaces.outcoming.gmaps.appKey=123"])
class GmapsServiceUnitTest {

    @Autowired
    lateinit var gmapsService: GmapsService

    @Autowired
    lateinit var mockWebClientBuilder: MockWebClientBuilder

    @Test
    @DirtiesContext
    fun `given a service that has the capability to look up distances between addresses, when I request the distance between two adresses, then I should get an int stating the result`() {

        mockWebClientBuilder.content = ClassPathResource("/responses/gmaps/ok_response_5_minutes.json").file.readText()

        val addressOne = randomAddress()
        val addressTwo = randomAddress()

        val distance = gmapsService.getDistanceBetweenAddresses(addressOne, addressTwo).block()!!

        assertEquals(308, distance)
        val uri = mockWebClientBuilder.getURI()
        assertTrue(uri.query.contains("origin=$addressOne", false))
        assertTrue(uri.query.contains("destination=$addressTwo", false))
        assertTrue(uri.query.contains("key=123", false))
    }

    @Test
    @DirtiesContext
    fun `given a service that has the capability to look up distances between addresses, when I do an invalid request, then I should get an exception`() {

        mockWebClientBuilder.content = ClassPathResource("/responses/gmaps/error_response.json").file.readText()

        val addressOne = randomAddress()
        val addressTwo = randomAddress()

        assertThrows<GMapsException> { gmapsService.getDistanceBetweenAddresses(addressOne, addressTwo).block() }
    }

    @Test
    @DirtiesContext
    fun `given a service that has the capability to look up distances between addresses, when I do a request that finds no results, then I should get distance as MAX_VALUE`() {

        mockWebClientBuilder.content = ClassPathResource("/responses/gmaps/ok_response_no_path_found.json").file.readText()

        val distance = gmapsService.getDistanceBetweenAddresses(randomAddress(), randomAddress()).block()!!

        assertEquals(Int.MAX_VALUE, distance)
    }
}

@Component
@Qualifier("GMaps")
class MockWebClientBuilder() : WebClient.Builder {

    lateinit var content: String

    private val listOfFunctions = mutableListOf<Function<UriBuilder, URI>>()

    override fun baseUrl(baseUrl: String) = this

    override fun defaultUriVariables(defaultUriVariables: MutableMap<String, *>) = this

    override fun uriBuilderFactory(uriBuilderFactory: UriBuilderFactory) = this

    override fun defaultHeader(header: String, vararg values: String?) = this

    override fun defaultHeaders(headersConsumer: Consumer<HttpHeaders>) = this

    override fun defaultCookie(cookie: String, vararg values: String?) = this

    override fun defaultCookies(cookiesConsumer: Consumer<MultiValueMap<String, String>>) = this

    override fun defaultRequest(defaultRequest: Consumer<WebClient.RequestHeadersSpec<*>>) = this

    override fun filter(filter: ExchangeFilterFunction) = this

    override fun filters(filtersConsumer: Consumer<MutableList<ExchangeFilterFunction>>) = this

    override fun clientConnector(connector: ClientHttpConnector) = this

    override fun codecs(configurer: Consumer<ClientCodecConfigurer>) = this

    override fun exchangeStrategies(strategies: ExchangeStrategies) = this

    override fun exchangeStrategies(configurer: Consumer<ExchangeStrategies.Builder>) = this

    override fun exchangeFunction(exchangeFunction: ExchangeFunction) = this

    override fun apply(builderConsumer: Consumer<WebClient.Builder>) = this

    override fun clone() = this

    override fun build(): WebClient {
        val webClient = mockk<WebClient>(relaxed = true)
        val requestHeadersUriSpecMock = mockk<RequestHeadersUriSpec<*>>()

        every { requestHeadersUriSpecMock.uri(capture(listOfFunctions)) } answers {
            callOriginal()
            requestHeadersUriSpecMock
        }
        val responseSpecMock = mockk<WebClient.ResponseSpec>()

        every { webClient.get() } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.retrieve() } returns responseSpecMock
        every { responseSpecMock.toEntity(String::class.java) } returns Mono.just(ResponseEntity.ok(content))

        return webClient
    }

    fun getURI(): URI {

        return listOfFunctions[0].apply(UriComponentsBuilder.newInstance())
    }
}
