package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.room.Converters
import com.simprints.id.data.db.event.local.models.DbEvent
import net.sqlcipher.database.SupportFactory

@Database(entities = [DbEvent::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
@Keep
abstract class EventRoomDatabase : RoomDatabase() {

    abstract val eventDao: EventRoomDao

    companion object {

        @Suppress("UNUSED_PARAMETER")
        fun getDatabase(context: Context,
                        factory: SupportFactory,
                        dbName: String,
                        crashReportManager: CrashReportManager): EventRoomDatabase =
            Room.databaseBuilder(context, EventRoomDatabase::class.java, dbName)
                .addMigrations()
                .addMigrations(EventMigration1to2(crashReportManager))
                .build()
    }

}
