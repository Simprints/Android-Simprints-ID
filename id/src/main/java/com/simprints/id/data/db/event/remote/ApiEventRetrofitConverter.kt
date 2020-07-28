package com.simprints.id.data.db.event.remote

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.remote.models.ApiEvent
import retrofit2.Converter
import java.lang.AssertionError
import java.lang.reflect.Type

class RetrofitDateSerializer : JsonSerializer<ApiEvent> {

    override fun serialize(srcDate: ApiEvent?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        if (srcDate == null)
            return null

        return JsonHelper.klaxon(
    }
}
