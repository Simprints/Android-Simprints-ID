package com.simprints.id.tools.json

import com.google.gson.*
import com.simprints.libsimprints.FingerIdentifier
import java.util.*

class JsonHelper {
    companion object {
        val gson: Gson by lazy {

            val builder = GsonBuilder()
            builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
                if (json.asJsonPrimitive.isNumber)
                    Date(json.asJsonPrimitive.asLong)
                else Date(json.asJsonPrimitive.asString)
            })

            builder.registerTypeAdapter(FingerIdentifier::class.java, JsonDeserializer<FingerIdentifier> { json, _, _ ->
                if (json.asJsonPrimitive.isNumber)
                    FingerIdentifier.values()[json.asJsonPrimitive.asInt]
                else FingerIdentifier.valueOf(json.asJsonPrimitive.asString)
            })

            builder.registerTypeAdapter(FingerIdentifier::class.java, JsonSerializer<FingerIdentifier> { src, _, _ ->
                JsonPrimitive(src.name)
            })

            val exclusionStrategy = object : ExclusionStrategy {
                override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                    return fieldAttributes.getAnnotation(SkipSerialisation::class.java) != null
                }

                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return false
                }
            }
            builder.addSerializationExclusionStrategy(exclusionStrategy)

            builder.create()
        }

        fun toJson(any: Any): String {
            return gson.toJson(any)
        }
    }
}
