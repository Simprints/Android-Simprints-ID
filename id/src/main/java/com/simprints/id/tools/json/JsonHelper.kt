package com.simprints.id.tools.json

import com.google.gson.*
import com.simprints.libsimprints.FingerIdentifier
import java.util.*

class JsonHelper {
    companion object {
        val gson: Gson by lazy {

            val builder = GsonBuilder()
            registerDateAdapter(builder)
            registerFingerIdentifierAdapter(builder)
            enablePostProcessing(builder)
            defineCustomStrategyToSkipSerialization(builder)
            builder.create()
        }

        private fun enablePostProcessing(builder: GsonBuilder) {
            builder.registerTypeAdapterFactory(PostProcessingEnabler())
        }

        private fun defineCustomStrategyToSkipSerialization(builder: GsonBuilder) {
            val exclusionStrategy = object : ExclusionStrategy {
                override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                    return fieldAttributes.getAnnotation(SkipSerialisationProperty::class.java) != null ||
                        fieldAttributes.getAnnotation(SkipSerialisationField::class.java) != null
                }

                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return false
                }
            }
            builder.addSerializationExclusionStrategy(exclusionStrategy)
        }

        private fun registerFingerIdentifierAdapter(builder: GsonBuilder) {
            builder.registerTypeAdapter(FingerIdentifier::class.java, JsonDeserializer<FingerIdentifier> { json, _, _ ->
                if (json.asJsonPrimitive.isNumber)
                    FingerIdentifier.values()[json.asJsonPrimitive.asInt]
                else FingerIdentifier.valueOf(json.asJsonPrimitive.asString)
            })

            builder.registerTypeAdapter(FingerIdentifier::class.java, JsonSerializer<FingerIdentifier> { src, _, _ ->
                JsonPrimitive(src.name)
            })
        }

        private fun registerDateAdapter(builder: GsonBuilder) {
            builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
                if (json.asJsonPrimitive.isNumber)
                    Date(json.asJsonPrimitive.asLong)
                else Date(json.asJsonPrimitive.asString)
            })
        }

        fun toJson(any: Any): String {
            return gson.toJson(any)
        }
    }
}
