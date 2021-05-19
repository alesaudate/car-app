package com.github.alesaudate.samples.reactive.carapp

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.io.File

@SpringBootTest
@ActiveProfiles("integration-test")
class CarAppApplicationTests {

    companion object {
        private val container = DockerComposeContainer<Nothing>(File("docker/docker-compose.yml"))

        @BeforeAll
        @JvmStatic
        fun init() {
            container.withExposedService("mysql_1", 3306)
            container.waitingFor("mysql_1", HostPortWaitStrategy())
            container.start()
        }

        @AfterAll
        fun destroy() {
            container.stop()
        }
    }

    @Test
    fun `given an empty context, when I try to start the system, then it should start correctly`() {
    }
}
