package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.db.event.local.models.DbEvent

@Database(entities = [DbEvent::class], version = 1, exportSchema = false)
@TypeConverters(DbEvent.Converters::class)
@Keep
abstract class EventRoomDatabase : RoomDatabase() {

    abstract val eventDao: EventRoomDao

    companion object {
        private const val ROOM_DB_NAME = "events_room_db"

        fun getDatabase(context: Context): EventRoomDatabase = Room
            .databaseBuilder(context.applicationContext, EventRoomDatabase::class.java, ROOM_DB_NAME)
            .build()
    }
}
