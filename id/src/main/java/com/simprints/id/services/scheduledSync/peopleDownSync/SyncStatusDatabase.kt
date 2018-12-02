package com.simprints.id.services.scheduledSync.peopleDownSync

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.db.DownSyncDao
import com.simprints.id.services.scheduledSync.peopleDownSync.db.DownSyncStatus
import com.simprints.id.services.scheduledSync.peopleUpsync.db.UpSyncDao
import com.simprints.id.services.scheduledSync.peopleUpsync.db.UpSyncStatus

@Database(entities = [DownSyncStatus::class, UpSyncStatus::class], version = 1, exportSchema = false)
abstract class SyncStatusDatabase : RoomDatabase() {

    abstract val downSyncStatusModel: DownSyncDao

    abstract val upSyncStatusModel: UpSyncDao

    companion object {
        private const val SYNC_STATUS_DB_NAME = "new_sync_status_db" // STOPSHIP : remove new from here and delete old

        fun getDatabase(context: Context): SyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, SyncStatusDatabase::class.java, SYNC_STATUS_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
}
