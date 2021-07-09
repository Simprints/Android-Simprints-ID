package com.simprints.eventsystem.event.local

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.eventsystem.BuildConfig
import com.simprints.eventsystem.common.Converters
import com.simprints.eventsystem.event.local.migrations.EventMigration1to2
import com.simprints.eventsystem.event.local.migrations.EventMigration2to3
import com.simprints.eventsystem.event.local.models.DbEvent
import net.sqlcipher.database.SupportFactory


@Database(entities = [DbEvent::class], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
@Keep
abstract class EventRoomDatabase : RoomDatabase() {

    abstract val eventDao: EventRoomDao

    companion object {

        fun getDatabase(
            context: Context,
            factory: SupportFactory,
            dbName: String
        ): EventRoomDatabase {
            val builder = Room.databaseBuilder(context, EventRoomDatabase::class.java, dbName)
                .addMigrations()
                .addMigrations(EventMigration1to2())
                .addMigrations(EventMigration2to3())

            if (BuildConfig.DB_ENCRYPTION)
                builder.openHelperFactory(factory)

            return builder.build()
        }

    }
}
