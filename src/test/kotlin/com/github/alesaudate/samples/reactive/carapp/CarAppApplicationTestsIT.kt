package com.github.alesaudate.samples.reactive.carapp

import com.github.alesaudate.samples.reactive.carapp.extensions.TestContainersExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
@ExtendWith(TestContainersExtension::class)
class CarAppApplicationTestsIT {

    @Test
    fun `given an empty context, when I try to start the system, then it should start correctly`() {
    }
}
