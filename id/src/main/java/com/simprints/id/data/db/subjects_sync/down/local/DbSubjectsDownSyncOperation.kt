package com.simprints.id.data.db.subjects_sync.down.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult.DownSyncState
import com.simprints.id.domain.modality.Modes

@Entity(tableName = "DbSubjectsDownSyncOperation")
@Keep
data class DbSubjectsDownSyncOperation(
    @PrimaryKey var id: DbSubjectsDownSyncOperationKey,
    var projectId: String,
    var userId: String? = null,
    var moduleId: String? = null,
    var modes: List<Modes> = emptyList(),
    var lastState: DownSyncState?,
    var lastEventId: String?,
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
        fun fromModesToString(modes: List<Modes>): String =
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
        fun fromDbSubjectsDownSyncOperationKeyToString(DbSubjectsDownSyncOperationKey: DbSubjectsDownSyncOperationKey): String =
            DbSubjectsDownSyncOperationKey.key

        @TypeConverter
        fun fromStringToDbSubjectsDownSyncOperationKey(key: String): DbSubjectsDownSyncOperationKey =
            DbSubjectsDownSyncOperationKey(key)
    }
}

data class DbSubjectsDownSyncOperationKey(val key: String) {

    companion object {
        const val SEPARATOR_PARAMS_KEY = "||"
    }

    constructor(projectId: String, modes: List<Modes>, userId: String? = null, moduleId: String? = null) : this(
        key = listOf(projectId, userId, moduleId, modes.joinToString(SEPARATOR_PARAMS_KEY)).joinToString(SEPARATOR_PARAMS_KEY)
    )
}

fun DbSubjectsDownSyncOperation.fromDbToDomain() =
    SubjectsDownSyncOperation(
        projectId,
        userId,
        moduleId,
        modes,
        lastState?.let {
            SubjectsDownSyncOperationResult(
                it,
                lastEventId,
                lastSyncTime
            )
        }
    )

fun DbSubjectsDownSyncOperation.isSameOperation(compareTo: SubjectsDownSyncOperation) =
    projectId == compareTo.projectId &&
    userId == compareTo.attendantId &&
    moduleId == compareTo.moduleId &&
    modes.toTypedArray() contentEquals compareTo.modes.toTypedArray()
