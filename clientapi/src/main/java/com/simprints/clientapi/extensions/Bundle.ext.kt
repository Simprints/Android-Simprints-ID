package com.simprints.clientapi.extensions

import android.os.Bundle
import org.json.JSONObject

fun Bundle.toJson(): String {
    val json = JSONObject()
    keySet().forEach { json.put(it, get(it)) }

    return json.toString()
}
