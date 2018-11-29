package com.simprints.id.data.db.sync.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SyncStatus::class], version = 1, exportSchema = false)
abstract class SyncStatusDatabase : RoomDatabase() {

    abstract val syncStatusModel: SyncStatusDao

    companion object {
        private const val SYNC_STATUS_DB_NAME = "sync_status_db"

        fun getDatabase(context: Context): SyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, SyncStatusDatabase::class.java, SYNC_STATUS_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
}
