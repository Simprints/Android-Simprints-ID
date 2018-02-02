package com.simprints.id.secure

import com.google.gson.Gson
import com.simprints.id.secure.domain.NonceScope

class JsonHelper {

    companion object {
        val gson by lazy {
            Gson()
        }

        fun toJson(nonceScope: NonceScope): String {
            return gson.toJson(nonceScope)
        }
    }
}
