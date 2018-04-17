package com.simprints.id.tools.json

import com.google.gson.*
import com.simprints.libsimprints.FingerIdentifier
import java.lang.reflect.Type

class FingerIdentifierAsIntJsonConverter : JsonSerializer<Int>, JsonDeserializer<Int> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Int {
        return if (json!!.asJsonPrimitive.isString) {
            FingerIdentifier.valueOf(json.asJsonPrimitive.asString).ordinal
        } else {
            json.asInt
        }
    }

    override fun serialize(src: Int, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(FingerIdentifier.values()[src].name)
    }
}
