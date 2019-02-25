package com.simprints.id.data.analytics.eventdata.controllers.remote.apiAdapters

import com.google.gson.*
import com.simprints.id.tools.extensions.removeIfExists
import java.lang.reflect.Type

class EventApiAdapter<T>(private val requiredGson: Gson = GsonBuilder().create()) : JsonSerializer<T> {
    override fun serialize(src: T, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return requiredGson.toJsonTree(src).asJsonObject.removeIfExists("id")
    }
}
