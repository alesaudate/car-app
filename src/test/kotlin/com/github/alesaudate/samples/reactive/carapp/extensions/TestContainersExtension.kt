package com.github.alesaudate.samples.reactive.carapp.extensions

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.io.File

class TestContainersExtension : BeforeAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        init()
    }

    companion object {
        private lateinit var container: DockerComposeContainer<Nothing>

        private var started = false

        @JvmStatic
        fun init() {
            if (! started) {
                warn("Starting containers for testing")
                container = DockerComposeContainer<Nothing>(File("docker/docker-compose.yml"))
                container.withExposedService("mysql_1", 3306)
                container.waitingFor("mysql_1", HostPortWaitStrategy())

                container.start()
                started = true
            }
        }
    }
}
