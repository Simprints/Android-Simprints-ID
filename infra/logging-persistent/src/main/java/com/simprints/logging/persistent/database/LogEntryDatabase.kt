package com.simprints.logging.persistent.database

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DbLogEntry::class],
    version = 1,
    exportSchema = false,
)
@Keep
internal abstract class LogEntryDatabase : RoomDatabase() {
    abstract val logDao: LogEntryDao

    companion object {
        private const val ROOM_DB_NAME = "persistent_log_db"

        fun getDatabase(context: Context): LogEntryDatabase = Room
            .databaseBuilder(
                context.applicationContext,
                LogEntryDatabase::class.java,
                ROOM_DB_NAME,
            ).fallbackToDestructiveMigration()
            .build()
    }
}
