package com.simprints.core.tools.json

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

object AnyPrimitiveSerializer : KSerializer<Any> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("AnyPrimitive", SerialKind.CONTEXTUAL)

    private fun toJsonElement(value: Any): JsonElement = when (value) {
        is String -> {
            JsonPrimitive(value)
        }

        is Number -> {
            JsonPrimitive(value)
        }

        is Boolean -> {
            JsonPrimitive(value)
        }

        is List<*> -> {
            JsonArray(
                value.map { v ->
                    v ?: throw SerializationException("Null values are not supported for AnyPrimitive")
                    toJsonElement(v)
                },
            )
        }

        is Map<*, *> -> {
            val obj = buildJsonObject {
                value.forEach { (k, v) ->
                    require(k is String) { "Only String keys are supported in maps for AnyPrimitiveSerializer" }
                    require(v != null) { "Null values are not supported for AnyPrimitive" }
                    put(k, toJsonElement(v))
                }
            }
            obj
        }

        else -> {
            throw SerializationException("Unsupported type for AnyPrimitiveSerializer: ${value::class}")
        }
    }

    private fun fromJsonElement(element: JsonElement): Any = when (element) {
        is JsonPrimitive -> {
            element.booleanOrNull?.let { return it }
            element.longOrNull?.let { return it }
            element.doubleOrNull?.let { return it }
            element.content
        }

        is JsonArray -> {
            element.map { fromJsonElement(it) }
        }

        is JsonObject -> {
            element.mapValues { (_, v) ->
                if (v is JsonNull) throw SerializationException("Null values are not supported for AnyPrimitive")
                fromJsonElement(v)
            }
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: Any,
    ) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("AnyPrimitiveSerializer can be used only with Json format")
        jsonEncoder.encodeJsonElement(toJsonElement(value))
    }

    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("AnyPrimitiveSerializer can be used only with Json format")
        val element = jsonDecoder.decodeJsonElement()
        return fromJsonElement(element)
    }
}
