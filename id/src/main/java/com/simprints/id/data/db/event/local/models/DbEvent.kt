package com.simprints.id.data.db.event.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.beust.klaxon.Klaxon
import com.google.gson.reflect.TypeToken
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.domain.modality.Modes
import java.util.*

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
        fun fromListOfStringToString(list: List<String>): String =
            JsonHelper.toJson(list)

        @TypeConverter
        fun fromStringToListOfString(jsonList: String): List<String> {
            val type = object : TypeToken<List<String>>() {}.type
            return JsonHelper.gson.fromJson(jsonList, type)
        }

        @TypeConverter
        fun fromListOfModesToString(list: List<Modes>): String =
            JsonHelper.toJson(list)

        @TypeConverter
        fun fromStringToListOfModes(jsonList: String): List<Modes> {
            val type = object : TypeToken<List<Modes>>() {}.type
            return JsonHelper.gson.fromJson(jsonList, type)
        }

        @TypeConverter
        fun toDate(dateLong: Long): Date {
            return Date(dateLong)
        }

        @TypeConverter
        fun fromDate(date: Date): Long {
            return date.time
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
        Klaxon().toJsonString(this),
        payload.createdAt,
        payload.endedAt
    )

fun DbEvent.fromDbToDomain(): Event =
    Klaxon().parse<Event>(eventJson) as Event
