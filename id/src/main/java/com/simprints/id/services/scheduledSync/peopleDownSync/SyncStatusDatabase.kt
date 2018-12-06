package com.simprints.id.services.scheduledSync.peopleDownSync

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncDao
import com.simprints.id.data.db.local.room.UpSyncStatus

@Database(entities = [DownSyncStatus::class, UpSyncStatus::class], version = 1, exportSchema = false)
abstract class SyncStatusDatabase : RoomDatabase() {

    abstract val downSyncDao: DownSyncDao

    abstract val upSyncDao: UpSyncDao

    companion object {
        private const val SYNC_STATUS_DB_NAME = "sync_status_db" // STOPSHIP : remove new from here and delete old

        fun getDatabase(context: Context): SyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, SyncStatusDatabase::class.java, SYNC_STATUS_DB_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}
