package com.github.alesaudate.samples.reactive.carapp.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

class OnionArchitectureTest {

    @Test
    fun `system must follow onion architecture guidelines`() {

        val importedClasses = ClassFileImporter()
            .importPackages("com.github.alesaudate.samples.reactive.carapp")

        val rule = layeredArchitecture()
            .layer("Domain").definedBy("..domain..")
            .layer("Outgoing").definedBy("..interfaces.outgoing..")
            .layer("Incoming").definedBy("..interfaces.incoming..")
            .layer("Fixtures").definedBy("..fixtures..")

            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Incoming", "Fixtures")
            .whereLayer("Outgoing").mayOnlyBeAccessedByLayers("Domain", "Fixtures")
            .whereLayer("Incoming").mayOnlyBeAccessedByLayers("Fixtures")

        rule.check(importedClasses)
    }
}
