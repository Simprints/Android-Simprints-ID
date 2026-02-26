package com.simprints.infra.protection.database

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simprints.infra.polyprotect.BuildConfig
import com.simprints.infra.protection.database.AuxDataDatabase.Companion.AUX_DB_VERSION
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [DbAuxData::class],
    version = AUX_DB_VERSION,
    exportSchema = false,
)
@Keep
internal abstract class AuxDataDatabase : RoomDatabase() {
    abstract val auxDataDao: AuxDataDao

    companion object {
        fun getDatabase(
            context: Context,
            factory: SupportOpenHelperFactory,
            dbName: String,
        ): AuxDataDatabase {
            val builder = Room.databaseBuilder(context, AuxDataDatabase::class.java, dbName)

            if (BuildConfig.DB_ENCRYPTION) {
                builder.openHelperFactory(factory)
            }

            return builder.build()
        }

        const val AUX_DB_VERSION = 1
    }
}
