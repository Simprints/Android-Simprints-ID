package com.simprints.id.data.db.event.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.domain.modality.Modes

@Entity
data class DbEvent(
    @PrimaryKey var id: String,

    @Embedded var labels: EventLabels,
    val type: EventType?,
    var eventJson: String,
    val createdAt: Long,
    val endedAt: Long
) {
    class Converters {

        @TypeConverter
        fun fromEventPayloadTypeToString(type: EventType?): String? =
            type?.name

        @TypeConverter
        fun fromStringToEventPayloadType(name: String?): EventType? =
            name?.let { EventType.valueOf(name) }

        @TypeConverter
        fun fromListOfStringToString(list: List<String>?): String? =
            list?.let { JsonHelper.jackson.writeValueAsString(list) }

        @TypeConverter
        fun fromStringToListOfString(jsonList: String?): List<String>? =
            jsonList?.let {
                val type = object : TypeReference<List<String>>() {}
                return JsonHelper.jackson.readValue(jsonList, type)
            }

        @TypeConverter
        fun fromListOfModesToString(list: List<Modes>?): String? =
            list?.let { JsonHelper.jackson.writeValueAsString(list) }

        @TypeConverter
        fun fromStringToListOfModes(jsonList: String?): List<Modes>? =
            jsonList?.let {
                val type = object : TypeReference<List<Modes>>() {}
                return JsonHelper.jackson.readValue(jsonList, type)
            }
    }

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}

fun Event.fromDomainToDb(): DbEvent =
    DbEvent(
        id,
        labels,
        payload.type,
        JsonHelper.jackson.writeValueAsString(this),
        payload.createdAt,
        payload.endedAt
    )

fun DbEvent.fromDbToDomain(): Event =
    JsonHelper.jackson.readValue(this.eventJson, object : TypeReference<Event>() {})
