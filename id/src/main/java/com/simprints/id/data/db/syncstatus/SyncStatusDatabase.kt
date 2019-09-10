package com.simprints.id.data.db.syncstatus

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.annotation.Keep
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncStatus
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncDao
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncStatus

@Database(entities = [DownSyncStatus::class, UpSyncStatus::class], version = 1, exportSchema = false)
@Keep
abstract class SyncStatusDatabase : RoomDatabase() {

    abstract val downSyncDao: DownSyncDao

    abstract val upSyncDao: UpSyncDao

    companion object {
        private const val SYNC_STATUS_DB_NAME = "sync_status_db"

        fun getDatabase(context: Context): SyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, SyncStatusDatabase::class.java, SYNC_STATUS_DB_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}
