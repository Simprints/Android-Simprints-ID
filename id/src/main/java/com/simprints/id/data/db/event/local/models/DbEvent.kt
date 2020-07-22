package com.simprints.id.data.db.event.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.beust.klaxon.Klaxon
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventType
import java.util.*

data class DbEventLabel(val type: String,
                        val eventLabelJson: String)

@Entity
data class DbEvent(
    @PrimaryKey var id: String,
    var labels: List<DbEventLabel>,
    val type: EventType,
    var eventJson: String,
    val addedAt: Date = Date()
) {
    class Converters {

        @TypeConverter
        fun fromEventLabelToString(labels: List<EventLabel>): String =
            JsonHelper.toJson(labels)

        @TypeConverter
        fun fromStringToEventLabel(jsonEventLabel: String): List<EventLabel> =
            JsonHelper.fromJson(jsonEventLabel)

        @TypeConverter
        fun fromEventPayloadTypeToString(type: EventType): String =
            type.name

        @TypeConverter
        fun fromStringToEventPayloadType(name: String): EventType =
            EventType.valueOf(name)

        @TypeConverter
        fun fromDbEventLabelToString(eventLabel: List<DbEventLabel>): String =
            JsonHelper.toJson(eventLabel)

        @TypeConverter
        fun fromStringToDbEventLabel(jsonEventLabel: String): List<DbEventLabel> =
            JsonHelper.fromJson(jsonEventLabel)


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
    DbEvent(id, labels.map { it.fromDomainToDb() }, payload.type,  Klaxon().toJsonString(this))

fun DbEvent.fromDbToDomain(): Event =
    Klaxon().parse<Event>(eventJson) as Event

fun EventLabel.fromDomainToDb() =
    DbEventLabel(this.key.name, JsonHelper.toJson(this))
