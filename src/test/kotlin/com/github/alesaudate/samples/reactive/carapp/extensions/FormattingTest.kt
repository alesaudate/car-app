package com.github.alesaudate.samples.reactive.carapp.extensions

import com.github.alesaudate.samples.reactive.carapp.randomDateInThePast
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.format.DateTimeFormatter

internal class FormattingTest {

    @Test
    fun `format to ISO date must return a ISO-pattern formatted date`() {

        val date = randomDateInThePast()
        val formattedDate = date.format(DateTimeFormatter.ISO_DATE)

        assertEquals(date.toIsoDate(), formattedDate)
    }
}
