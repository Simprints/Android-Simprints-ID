package com.simprints.id.tools.json

import com.google.gson.*
import com.simprints.libcommon.Utils
import java.lang.reflect.Type

class TemplateAsByteArrayJsonConverter : JsonSerializer<ByteArray>, JsonDeserializer<ByteArray> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ByteArray {
        return Utils.base64ToBytes(json?.asJsonPrimitive?.asString ?: "")
    }

    override fun serialize(src: ByteArray, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(Utils.byteArrayToBase64(src))
    }
}
