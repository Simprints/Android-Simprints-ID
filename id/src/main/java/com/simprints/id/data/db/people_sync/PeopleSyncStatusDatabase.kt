package com.simprints.id.data.db.people_sync

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncOperationLocalDataSource
import com.simprints.id.data.db.people_sync.up.local.DbUpSyncOperation
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncOperationLocalDataSource

@Database(entities = [DbPeopleDownSyncOperation::class, DbUpSyncOperation::class], version = 1, exportSchema = false)
@TypeConverters(DbPeopleDownSyncOperation.Converters::class, DbUpSyncOperation.Converters::class)
@Keep
abstract class PeopleSyncStatusDatabase : RoomDatabase() {

    abstract val downSyncOperationOperationDataSource: PeopleDownSyncOperationLocalDataSource

    abstract val upSyncOperationLocalDataSource: PeopleUpSyncOperationLocalDataSource

    companion object {
        private const val ROOM_DB_NAME = "room_db"

        fun getDatabase(context: Context): PeopleSyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, PeopleSyncStatusDatabase::class.java, ROOM_DB_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}
