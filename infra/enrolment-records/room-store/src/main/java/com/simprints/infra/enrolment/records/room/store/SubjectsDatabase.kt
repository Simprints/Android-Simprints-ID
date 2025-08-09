package com.simprints.infra.enrolment.records.room.store

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.infra.enrolment.records.room.store.BuildConfig
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbCommCareCase
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        DbSubject::class,
        DbBiometricTemplate::class,
        DbCommCareCase::class,
    ],
    version = 2,
    exportSchema = true,
)
@Keep
abstract class SubjectsDatabase : RoomDatabase() {
    abstract val subjectDao: SubjectDao
    abstract val commCareCaseDao: CommCareCaseDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `DbCommCareCase` (
                        `caseId` TEXT NOT NULL PRIMARY KEY,
                        `subjectId` TEXT NOT NULL,
                        `lastModified` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_DbCommCareCase_caseId` ON `DbCommCareCase` (`caseId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_DbCommCareCase_subjectId` ON `DbCommCareCase` (`subjectId`)")
            }
        }

        fun getDatabase(
            context: Context,
            factory: SupportOpenHelperFactory,
            dbName: String,
        ): SubjectsDatabase {
            val builder = Room.databaseBuilder(context, SubjectsDatabase::class.java, dbName)
                .addMigrations(MIGRATION_1_2)
            if (BuildConfig.DB_ENCRYPTION) {
                builder.openHelperFactory(factory)
            }
            return builder.build()
        }
    }
}
