package com.simprints.infra.events.event.local

import androidx.room.TypeConverter
import com.simprints.infra.events.event.domain.models.EventType

internal class Converters {
    @TypeConverter
    fun fromEventPayloadTypeToString(type: EventType?): String? = type?.name

    @TypeConverter
    fun fromStringToEventPayloadType(name: String?): EventType? = name?.let { EventType.valueOf(name) }
}
