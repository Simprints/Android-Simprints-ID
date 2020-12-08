package com.simprints.id.data.db.events_sync

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.db.common.room.Converters
import com.simprints.id.data.db.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.id.data.db.events_sync.down.local.DbEventsDownSyncOperationState
import com.simprints.id.data.db.events_sync.up.local.DbEventUpSyncOperationStateDao
import com.simprints.id.data.db.events_sync.up.local.DbEventsUpSyncOperationState

@Database(entities = [DbEventsDownSyncOperationState::class, DbEventsUpSyncOperationState::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
@Keep
abstract class EventSyncStatusDatabase : RoomDatabase() {

    abstract val downSyncOperationsDao: DbEventDownSyncOperationStateDao

    abstract val upSyncOperationsDaoDb: DbEventUpSyncOperationStateDao

    companion object {
        private const val ROOM_DB_NAME = "room_db"

        fun getDatabase(context: Context): EventSyncStatusDatabase = Room
            .databaseBuilder(context.applicationContext, EventSyncStatusDatabase::class.java, ROOM_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
}
