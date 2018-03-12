package com.simprints.id.tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import java.util.*

class JsonHelper {
    companion object {

        fun create(): Gson {
            val builder = GsonBuilder()
            builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ -> Date(json.asJsonPrimitive.asLong) })
            return builder.create()
        }
    }
}
