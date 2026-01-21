package com.simprints.core.tools.utils

import com.simprints.core.tools.extentions.toJsonElementMap
import com.simprints.core.tools.extentions.toStringMap
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class MapUtilKtTest {
    @Test
    fun `toStringMap JsonPrimitive string is returned as content`() {
        val input = mapOf(
            "key" to JsonPrimitive("value"),
        )

        val result = input.toStringMap()

        assertEquals(mapOf("key" to "value"), result)
    }

    @Test
    fun `toStringMap JsonPrimitive number is returned as string`() {
        val input = mapOf(
            "key" to JsonPrimitive(42),
        )

        val result = input.toStringMap()

        assertEquals(mapOf("key" to "42"), result)
    }

    @Test
    fun `toStringMap JsonObject is converted using toString`() {
        val jsonObject = buildJsonObject {
            put("a", 1)
        }
        val input = mapOf(
            "key" to jsonObject,
        )

        val result = input.toStringMap()

        assertEquals(mapOf("key" to jsonObject.toString()), result)
    }

    @Test
    fun `toStringMap JsonArray is converted using toString`() {
        val jsonArray = buildJsonArray {
            add(1)
            add(2)
        }
        val input = mapOf(
            "key" to jsonArray,
        )

        val result = input.toStringMap()

        assertEquals(mapOf("key" to jsonArray.toString()), result)
    }

    @Test
    fun `toStringMap null JsonElement is converted to string null`() {
        val input = mapOf(
            "key" to null,
        )

        val result = input.toStringMap()

        assertEquals(mapOf("key" to ""), result)
    }

    @Test
    fun `toJsonElementMap String is converted to JsonPrimitive string`() {
        val input = mapOf(
            "key" to "value",
        )

        val result = input.toJsonElementMap()

        assertEquals(JsonPrimitive("value"), result["key"])
    }

    @Test
    fun `toJsonElementMap Boolean is converted to JsonPrimitive boolean`() {
        val input = mapOf(
            "key" to true,
        )

        val result = input.toJsonElementMap()

        assertEquals(JsonPrimitive(true), result["key"])
    }

    @Test
    fun `toJsonElementMap Number is converted to JsonPrimitive number`() {
        val input = mapOf(
            "key" to 123,
        )

        val result = input.toJsonElementMap()

        assertEquals(JsonPrimitive(123), result["key"])
    }

    @Test
    fun `toJsonElementMap Unknown object is converted using toString`() {
        val value = object {
            override fun toString() = "custom-object"
        }
        val input = mapOf(
            "key" to value,
        )

        val result = input.toJsonElementMap()

        assertEquals(JsonPrimitive("custom-object"), result["key"])
    }

    @Test
    fun `toJsonElementMap null value is converted to JsonPrimitive string null`() {
        val input = mapOf(
            "key" to null,
        )

        val result = input.toJsonElementMap()

        assertEquals(JsonPrimitive(""), result["key"])
    }
}
