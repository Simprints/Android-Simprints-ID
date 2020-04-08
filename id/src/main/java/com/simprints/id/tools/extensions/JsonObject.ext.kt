package com.simprints.id.tools.extensions
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.getString(field: String): String = this.get(field).asString
