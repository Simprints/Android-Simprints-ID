package com.simprints.core.tools.json

import com.simprints.testtools.common.syntax.assertThrows
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
        assertThrows<Throwable> {
            JsonHelper.validateJsonOrThrow(json)
        }
    }
}
