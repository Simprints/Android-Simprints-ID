package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownSyncStatus::class, UpSyncStatus::class], version = 1, exportSchema = false)
abstract class NewSyncStatusDatabase : RoomDatabase() {

    abstract val downSyncStatusModel: DownSyncDao

    abstract val upSyncStatusModel: UpSyncDao

    companion object {
        private const val SYNC_STATUS_DB_NAME = "new_sync_status_db" // STOPSHIP : remove new from here and delete old

        fun getDatabase(context: Context): NewSyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, NewSyncStatusDatabase::class.java, SYNC_STATUS_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
}
