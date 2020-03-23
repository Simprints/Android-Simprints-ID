package com.simprints.core.tools.json

import androidx.annotation.Keep
import com.google.gson.*
import com.simprints.libsimprints.FingerIdentifier
import java.util.*

@Keep
class JsonHelper {
    companion object {
        val gson: Gson by lazy {

            val builder = GsonBuilder()
            registerDateAdapter(builder)
            registerFingerIdentifierAdapter(builder)
            builder.create()
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
                else 
                    Date(json.asJsonPrimitive.asString) //TODO: find a replacement for deprecated method
            })

            builder.registerTypeAdapter(Date::class.java, JsonSerializer<Date> { src, _, _ ->
                JsonPrimitive(src.time)
            })
        }

        fun toJson(any: Any): String {
            return gson.toJson(any)
        }

        inline fun <reified T> fromJson(json: String): T {
            return gson.fromJson(json, T::class.java)
        }
    }
}
