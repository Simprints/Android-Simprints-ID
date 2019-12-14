package com.simprints.id.data.db.people_sync.up.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "UpSyncStatus")
@Keep
data class DbUpSyncOperation(
    @PrimaryKey val id: DbUpSyncOperationKey,
    val projectId: String,
    val lastSyncTime: Long? = null
) {

    class Converters {
        @TypeConverter
        fun fromDbUpSyncOperationKeyToString(dbUpSyncOperationKey: DbUpSyncOperationKey): String =
            dbUpSyncOperationKey.key

        @TypeConverter
        fun fromStringToDbUpSyncOperationKey(key: String): DbUpSyncOperationKey =
            DbUpSyncOperationKey(key)
    }
}

class DbUpSyncOperationKey(val key: String)
