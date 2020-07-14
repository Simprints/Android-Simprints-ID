package com.simprints.id.data.db.event.local.models

import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.events.Event.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload

data class DbEvent(
    @PrimaryKey var id: String,
    var labels: List<EventLabel>,
    var payload: EventPayload
) {

    constructor() : this() {}

    class Converters {
        @TypeConverter
        fun fromEventLabelToString(label: EventLabel): String =
            JsonHelper.toJson(label)

        @TypeConverter
        fun fromStringToEventLabel(jsonEventLabel: String): EventLabel =
            JsonHelper.fromJson(jsonEventLabel)

        @TypeConverter
        fun fromPayloadToString(payload: EventPayload): String =
            JsonHelper.toJson(payload)

        @TypeConverter
        fun fromStringToPayload(jsonPayload: String): EventPayload =
            JsonHelper.fromJson(jsonPayload)
    }

    constructor(event: Event) : this() {
        id = event.id
        payloadType = event.payload.type
        jsonEvent = JsonHelper.toJson(event)
    }
}

//constructor(event: Event) : this() {
//    id = event.id
//    payloadType = event.payload.type
//    jsonEvent = JsonHelper.toJson(event)
//}

//
//data class DbSubjectsDownSyncOperation(
//    @androidx.room.PrimaryKey var id: DbSubjectsDownSyncOperationKey,
//    var projectId: String,
//    var userId: String? = null,
//    var moduleId: String? = null,
//    var modes: List<Modes> = emptyList(),
//    var lastState: DownSyncState?,
//    var lastEventId: String?,
//    var lastSyncTime: Long? = null
//)
