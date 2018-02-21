package com.simprints.id.secure

import com.google.gson.Gson

class JsonHelper {

    companion object {
        val gson by lazy {
            Gson()
        }

        fun toJson(any: Any): String {
            return gson.toJson(any)
        }
    }
}
