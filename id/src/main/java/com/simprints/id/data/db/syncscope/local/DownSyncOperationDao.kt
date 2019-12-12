package com.simprints.id.data.db.syncstatus.downsyncinfo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.id.data.db.syncscope.local.DbDownSyncOperation
import com.simprints.id.domain.modality.Modes

@Dao
interface DownSyncOperationDao {

    @Query("select * from DbDownSyncOperation")
    suspend fun getDownSyncOperation(): List<DbDownSyncOperation>

    @Query("select * from DbDownSyncOperation where projectId = :projectId AND modes = :modes AND userId = :userId AND moduleId = :moduleId")
    suspend fun getDownSyncOperation(projectId: String, modes: List<Modes>, userId: String? = null, moduleId: String? = null): List<DbDownSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDownSyncOperation(dbDownSyncOperation: DbDownSyncOperation)

    @Query("delete from DbDownSyncOperation")
    suspend fun deleteAll()
}
