package com.simprints.id.data.db.common.room

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.id.data.db.events_sync.down.domain.RemoteEventQuery
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState
import com.simprints.id.data.db.events_sync.up.domain.LocalEventQuery
import com.simprints.id.domain.modality.Modes

class Converters {

    private val jsonHelper = JsonHelper()

    @TypeConverter
    fun fromEventPayloadTypeToString(type: EventType?): String? =
        type?.name

    @TypeConverter
    fun fromStringToEventPayloadType(name: String?): EventType? =
        name?.let { EventType.valueOf(name) }

    @TypeConverter
    fun fromListOfStringToString(list: List<String>?): String? =
        list?.let { jsonHelper.toJson(list) }

    @TypeConverter
    fun fromStringToListOfString(jsonList: String?): List<String>? =
        jsonList?.let {
            val type = object : TypeReference<List<String>>() {}
            return  jsonHelper.fromJson(jsonList, type)
        }

    @TypeConverter
    fun fromListOfModesToString(list: List<Modes>?): String? =
        list?.let { jsonHelper.toJson(list) }

    @TypeConverter
    fun fromStringToListOfModes(jsonList: String?): List<Modes>? =
        jsonList?.let {
            val type = object : TypeReference<List<Modes>>() {}
            return jsonHelper.fromJson(jsonList, type)
        }

    @TypeConverter
    fun fromEventDownSyncOperationToString(op: EventDownSyncOperation): String =
        jsonHelper.toJson(op)

    @TypeConverter
    fun fromStringToEventDownSyncOperation(json: String): EventDownSyncOperation {
        val type = object : TypeReference<EventDownSyncOperation>() {}
        return jsonHelper.fromJson(json, type)
    }

    @TypeConverter
    fun fromEventUpSyncOperationToString(op: EventUpSyncOperation): String =
        jsonHelper.toJson(op)

    @TypeConverter
    fun fromStringToEventUpSyncOperation(json: String): EventUpSyncOperation {
        val type = object : TypeReference<EventUpSyncOperation>() {}
        return jsonHelper.fromJson(json, type)
    }

    @TypeConverter
    fun fromLocalEventQueryToString(value: LocalEventQuery): String =
        jsonHelper.toJson(value)

    @TypeConverter
    fun fromStringToLocalEventQuery(json: String): LocalEventQuery {
        val type = object : TypeReference<LocalEventQuery>() {}
        return jsonHelper.fromJson(json, type)
    }

    @TypeConverter
    fun fromRemoteEventQueryToString(value: RemoteEventQuery): String =
        jsonHelper.toJson(value)

    @TypeConverter
    fun fromStringToRemoteEventQuery(json: String): RemoteEventQuery {
        val type = object : TypeReference<RemoteEventQuery>() {}
        return jsonHelper.fromJson(json, type)
    }

    @TypeConverter
    fun fromDownSyncStateToString(state: DownSyncState): String =
        state.name

    @TypeConverter
    fun fromStringToDownSyncState(name: String): DownSyncState =
        name.let { DownSyncState.valueOf(name) }

    @TypeConverter
    fun fromUpSyncStateToString(state: UpSyncState): String =
        state.name

    @TypeConverter
    fun fromStringToUpSyncState(name: String): UpSyncState =
        name.let { UpSyncState.valueOf(name) }
}
