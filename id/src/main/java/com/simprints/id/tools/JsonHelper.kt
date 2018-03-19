package com.simprints.id.tools

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import java.util.*

class JsonHelper {
    companion object {
        val gson by lazy {

            val builder = GsonBuilder()
            builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ -> Date(json.asJsonPrimitive.asLong) })
            builder.create()
        }

        fun toJson(any: Any): String {
            return gson.toJson(any)
        }
    }
}
