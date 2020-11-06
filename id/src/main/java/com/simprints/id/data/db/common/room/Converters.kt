package com.simprints.id.data.db.common.room

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState
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
