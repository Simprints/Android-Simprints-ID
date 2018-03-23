package com.simprints.id.tools

import com.google.gson.*
import com.simprints.libsimprints.FingerIdentifier
import java.util.*

class JsonHelper {
    companion object {
        val gson: Gson by lazy {

            val builder = GsonBuilder()
            builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ -> Date(json.asJsonPrimitive.asLong) })
            builder.registerTypeAdapter(Date::class.java, JsonSerializer<Date> { src, _, _ ->
                if (src == null) null else JsonPrimitive(src.time)
            })

            builder.registerTypeAdapter(FingerIdentifier::class.java, JsonSerializer<FingerIdentifier> { src, _, _ ->
                if (src == null) null else JsonPrimitive(src.name)
            })

            builder.create()
        }

        fun toJson(any: Any): String {
            return gson.toJson(any)
        }
    }
}
