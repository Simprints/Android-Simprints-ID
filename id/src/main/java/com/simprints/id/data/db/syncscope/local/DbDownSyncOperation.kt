package com.simprints.id.data.db.syncscope.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.simprints.id.data.db.syncscope.domain.DownSyncInfo
import com.simprints.id.data.db.syncscope.domain.DownSyncOperation
import com.simprints.id.domain.modality.Modes

@Entity(tableName = "DbDownSyncOperation")
@Keep
data class DbDownSyncOperation(
    @PrimaryKey var id: String,
    var projectId: String,
    var userId: String? = null,
    var moduleId: String? = null,
    var modes: List<Modes> = emptyList(),
    var lastState: DownSyncInfo.DownSyncState?,
    var lastPatientId: String?,
    var lastPatientUpdatedAt: Long?,
    var lastSyncTime: Long? = null
) {
    class Converters {

        companion object {
            const val MODES_STRING_SEPARATOR = "||"
        }

        @TypeConverter
        fun fromStringToModes(stringStored: String): List<Modes> =
            stringStored.split(MODES_STRING_SEPARATOR).map {
                Modes.valueOf(it)
            }

        @TypeConverter
        fun fromModeToString(modes: List<Modes>): String =
            modes.joinToString(separator = MODES_STRING_SEPARATOR) { it.name }

        @TypeConverter
        fun fromStringToDownSyncState(string: String): DownSyncInfo.DownSyncState =
            DownSyncInfo.DownSyncState.valueOf(string)

        @TypeConverter
        fun fromDownSyncStateToString(downSyncState: DownSyncInfo.DownSyncState): String =
            downSyncState.toString()
    }
}

fun DbDownSyncOperation.fromDbToDomain() =
    DownSyncOperation(
        projectId,
        userId,
        moduleId,
        modes,
        lastState?.let {
            DownSyncInfo(
                it,
                lastPatientId,
                lastPatientUpdatedAt,
                lastSyncTime
            )
        }

    )
