package com.simprints.id.tools.extensions

import com.google.gson.JsonObject

fun JsonObject.removeIfExists(field: String): JsonObject {
    if (this.has(field)) {
        this.remove(field)
    }
    return this
}

fun JsonObject.getString(field: String): String = this.get(field).asString
