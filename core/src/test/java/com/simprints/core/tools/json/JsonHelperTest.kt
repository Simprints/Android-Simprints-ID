package com.simprints.core.tools.json

import io.kotlintest.shouldThrow
import org.junit.Test

class JsonHelperTest {

    @Test
    fun validateJson() {
        val json = """{"name": "Test" }"""
        JsonHelper.validateJsonOrThrow(json)
    }

    @Test
    fun validateJson_shouldThrownIfInvalid() {
        val json = """{"name": "Test }"""
        shouldThrow<Throwable> {
            JsonHelper.validateJsonOrThrow(json)
        }
    }
}
