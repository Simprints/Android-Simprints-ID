package com.simprints.infra.events.event.local

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simprints.infra.events.BuildConfig
import com.simprints.infra.events.event.local.migrations.*
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.DbEventScope
import com.simprints.infra.events.local.migrations.*
import net.sqlcipher.database.SupportFactory


@Database(
    entities = [
        DbEvent::class,
        DbEventScope::class,
    ],
    version = 15,
    exportSchema = true
)
@TypeConverters(Converters::class)
@Keep
internal abstract class EventRoomDatabase : RoomDatabase() {

    abstract val eventDao: EventRoomDao

    abstract val scopeDao: SessionScopeRoomDao

    companion object {

        fun getDatabase(
            context: Context,
            factory: SupportFactory,
            dbName: String,
        ): EventRoomDatabase {
            val builder = Room.databaseBuilder(context, EventRoomDatabase::class.java, dbName)
                .addMigrations(EventMigration1to2())
                .addMigrations(EventMigration2to3())
                .addMigrations(EventMigration3to4())
                .addMigrations(EventMigration4to5())
                .addMigrations(EventMigration5to6())
                .addMigrations(EventMigration6to7())
                .addMigrations(EventMigration7to8())
                .addMigrations(EventMigration8to9())
                .addMigrations(EventMigration9to10())
                .addMigrations(EventMigration10to11())
                .addMigrations(EventMigration11to12())
                .addMigrations(EventMigration12to13())
                .addMigrations(EventMigration13to14())
                .addMigrations(EventMigration14to15())

            if (BuildConfig.DB_ENCRYPTION)
                builder.openHelperFactory(factory)

            return builder.build()
        }

    }
}
