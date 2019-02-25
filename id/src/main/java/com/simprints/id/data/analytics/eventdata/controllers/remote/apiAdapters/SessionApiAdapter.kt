package com.simprints.id.data.analytics.eventdata.controllers.remote.apiAdapters

import com.google.gson.*
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.tools.extensions.removeIfExists
import java.lang.reflect.Type

class SessionApiAdapter(private val requiredGson: Gson = GsonBuilder().create()) : JsonSerializer<SessionEvents> {
    override fun serialize(src: SessionEvents, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return requiredGson.toJsonTree(src).asJsonObject.removeIfExists("projectId")
    }
}
