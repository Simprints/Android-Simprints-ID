package com.simprints.id.data.db.people_sync

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperationDao
import com.simprints.id.data.db.people_sync.up.local.DbUpSyncOperation
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncDao

@Database(entities = [DbPeopleDownSyncOperation::class, DbUpSyncOperation::class], version = 1, exportSchema = false)
@TypeConverters(DbPeopleDownSyncOperation.Converters::class, DbUpSyncOperation.Converters::class)
@Keep
abstract class PeopleSyncStatusDatabase : RoomDatabase() {

    abstract val downSyncOperationOperationDaoDb: DbPeopleDownSyncOperationDao

    abstract val upSyncDao: PeopleUpSyncDao

    companion object {
        private const val SYNC_STATUS_DB_NAME = "people_sync_db"

        fun getDatabase(context: Context): PeopleSyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, PeopleSyncStatusDatabase::class.java, SYNC_STATUS_DB_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}
