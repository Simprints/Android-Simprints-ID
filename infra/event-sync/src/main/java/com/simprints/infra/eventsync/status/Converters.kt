package com.simprints.infra.eventsync.status

import androidx.room.TypeConverter
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState

internal class Converters {
    @TypeConverter
    fun fromDownSyncStateToString(state: DownSyncState): String = state.name

    @TypeConverter
    fun fromStringToDownSyncState(name: String): DownSyncState = name.let { DownSyncState.valueOf(name) }

    @TypeConverter
    fun fromUpSyncStateToString(state: UpSyncState): String = state.name

    @TypeConverter
    fun fromStringToUpSyncState(name: String): UpSyncState = name.let { UpSyncState.valueOf(name) }
}
