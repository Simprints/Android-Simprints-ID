package com.simprints.id.data.db.syncscope.local

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncOperationDao
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncDao
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncStatus

@Database(entities = [DbDownSyncOperation::class, UpSyncStatus::class], version = 1, exportSchema = false)
@TypeConverters(DbDownSyncOperation.Converters::class)
@Keep
abstract class SyncStatusDatabase : RoomDatabase() {

    abstract val downSyncOperationDao: DownSyncOperationDao

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
