package com.simprints.id.data.db.subjects_sync

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.db.subjects_sync.down.local.DbSubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.local.SubjectsDownSyncOperationLocalDataSource
import com.simprints.id.data.db.subjects_sync.up.local.DbUpSyncOperation
import com.simprints.id.data.db.subjects_sync.up.local.SubjectsUpSyncOperationLocalDataSource

@Database(entities = [DbSubjectsDownSyncOperation::class, DbUpSyncOperation::class], version = 2, exportSchema = false)
@TypeConverters(DbSubjectsDownSyncOperation.Converters::class, DbUpSyncOperation.Converters::class)
@Keep
abstract class SubjectsSyncStatusDatabase : RoomDatabase() {

    abstract val downSyncOperationOperationDataSource: SubjectsDownSyncOperationLocalDataSource

    abstract val upSyncOperationLocalDataSource: SubjectsUpSyncOperationLocalDataSource

    companion object {
        private const val ROOM_DB_NAME = "room_db"

        fun getDatabase(context: Context): SubjectsSyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, SubjectsSyncStatusDatabase::class.java, ROOM_DB_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}
