package com.github.alesaudate.samples.reactive.carapp.extensions

import org.apache.commons.text.StringSubstitutor
import org.springframework.core.io.ClassPathResource

fun loadFileContents(file: String, variables: Map<String, String> = emptyMap()): String {
    val fileContents = ClassPathResource(file).inputStream.readAllBytes().decodeToString()
    return StringSubstitutor(variables).replace(fileContents)
}
