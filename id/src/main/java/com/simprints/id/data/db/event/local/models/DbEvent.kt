package com.simprints.id.data.db.event.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.*
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

data class DbEventLabel(val type: String,
                        val eventLabelJson: String)

@Entity
data class DbEvent(
    @PrimaryKey var id: String,
    var labels: List<DbEventLabel>,
    val type: EventPayloadType,
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
        fun fromEventPayloadTypeToString(type: EventPayloadType): String =
            type.name

        @TypeConverter
        fun fromStringToEventPayloadType(name: String): EventPayloadType =
            EventPayloadType.valueOf(name)

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
}

fun Event.fromDomainToDb() =
    DbEvent(id, labels.map { it.fromDomainToDb() }, payload.type, JsonHelper.toJson(this))

fun EventLabel.fromDomainToDb() =
    when(this) {
        is ProjectId -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
        is SubjectId -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
        is AttendantId -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
        is ModuleId -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
        is Mode -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
        is SessionId -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
        is DeviceId -> DbEventLabel(this.javaClass.simpleName, JsonHelper.toJson(this))
    }
