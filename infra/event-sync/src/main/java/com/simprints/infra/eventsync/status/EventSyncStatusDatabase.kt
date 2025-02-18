package com.simprints.infra.eventsync.status

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.down.local.DbEventsDownSyncOperationState
import com.simprints.infra.eventsync.status.up.local.DbEventUpSyncOperationStateDao
import com.simprints.infra.eventsync.status.up.local.DbEventsUpSyncOperationState

@Database(
    entities = [
        DbEventsDownSyncOperationState::class,
        DbEventsUpSyncOperationState::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
@Keep
internal abstract class EventSyncStatusDatabase : RoomDatabase() {
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
