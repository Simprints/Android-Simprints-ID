package com.simprints.id.data.db.people_sync.up.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState

@Entity(tableName = "UpSyncStatus")
@Keep
data class DbUpSyncOperation(
    @PrimaryKey val id: DbUpSyncOperationKey,
    val projectId: String,
    val lastState: UpSyncState?,
    val lastSyncTime: Long? = null
) {

    class Converters {
        @TypeConverter
        fun fromDbUpSyncOperationKeyToString(dbUpSyncOperationKey: DbUpSyncOperationKey): String =
            dbUpSyncOperationKey.key

        @TypeConverter
        fun fromStringToDbUpSyncOperationKey(key: String): DbUpSyncOperationKey =
            DbUpSyncOperationKey(key)

        @TypeConverter
        fun fromStringToUpSyncState(string: String?): UpSyncState? =
            string?.let {
                UpSyncState.valueOf(it)
            }

        @TypeConverter
        fun fromUpSyncStateToString(upSyncState: UpSyncState?): String? =
            upSyncState?.toString()

    }
}

class DbUpSyncOperationKey(val key: String)

fun DbUpSyncOperation.fromDbToDomain() =
    PeopleUpSyncOperation(
        projectId,
        lastState?.let {
            PeopleUpSyncOperationResult(it, lastSyncTime)
        })
