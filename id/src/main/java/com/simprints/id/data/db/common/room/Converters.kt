package com.simprints.id.data.db.common.room

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperationResult.DownSyncState
import com.simprints.id.data.db.events_sync.down.local.DbEventsDownSyncOperationKey
import com.simprints.id.domain.modality.Modes

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

    @TypeConverter
    fun fromStringToDownSyncState(string: String?): DownSyncState? =
        string?.let {
            DownSyncState.valueOf(it)
        }

    @TypeConverter
    fun fromDownSyncStateToString(downSyncState: DownSyncState?): String? =
        downSyncState?.toString()

    @TypeConverter
    fun fromDbEventsDownSyncOperationKeyToString(DbEventsDownSyncOperationKey: DbEventsDownSyncOperationKey): String =
        DbEventsDownSyncOperationKey.key

    @TypeConverter
    fun fromStringToDbEventsDownSyncOperationKey(key: String): DbEventsDownSyncOperationKey =
        DbEventsDownSyncOperationKey(key)
}
