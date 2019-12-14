package com.simprints.id.data.db.people_sync.down.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState
import com.simprints.id.domain.modality.Modes

@Entity(tableName = "DbDownSyncOperation")
@Keep
data class DbDownSyncOperation(
    @PrimaryKey var id: DbDownSyncOperationKey,
    var projectId: String,
    var userId: String? = null,
    var moduleId: String? = null,
    var modes: List<Modes> = emptyList(),
    var lastState: DownSyncState?,
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
        fun fromStringToDownSyncState(string: String?): DownSyncState? =
            string?.let {
                DownSyncState.valueOf(it)
            }

        @TypeConverter
        fun fromDownSyncStateToString(downSyncState: DownSyncState?): String? =
            downSyncState?.toString()

        @TypeConverter
        fun fromDbDownSyncOperationKeyToString(dbDownSyncOperationKey: DbDownSyncOperationKey): String =
            dbDownSyncOperationKey.key

        @TypeConverter
        fun fromStringToDbDownSyncOperationKey(key: String): DbDownSyncOperationKey =
            DbDownSyncOperationKey(key)
    }
}

class DbDownSyncOperationKey(val key: String) {

    companion object {
        const val SEPARATOR_PARAMS_KEY = "||"
    }

    constructor(projectId: String, modes: List<Modes>, userId: String? = null, moduleId: String? = null) : this(
        key = listOf(projectId, userId, moduleId, modes.joinToString(SEPARATOR_PARAMS_KEY)).joinToString(SEPARATOR_PARAMS_KEY)
    )
}

fun DbDownSyncOperation.fromDbToDomain() =
    PeopleDownSyncOperation(
        projectId,
        userId,
        moduleId,
        modes,
        lastState?.let {
            PeopleDownSyncOperationResult(
                it,
                lastPatientId,
                lastPatientUpdatedAt,
                lastSyncTime
            )
        }
    )
