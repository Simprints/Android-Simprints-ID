package com.simprints.infra.enrolment.records.room.store

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase.Companion.SUBJECT_DB_VERSION
import com.simprints.infra.enrolment.records.room.store.migration.SubjectMigration1to2
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        DbSubject::class,
        DbBiometricTemplate::class,
        DbExternalCredential::class,
    ],
    version = SUBJECT_DB_VERSION,
    exportSchema = true,
)
@Keep
abstract class SubjectsDatabase : RoomDatabase() {
    abstract val subjectDao: SubjectDao

    companion object {
        fun getDatabase(
            context: Context,
            factory: SupportOpenHelperFactory,
            dbName: String,
        ): SubjectsDatabase {
            val builder = Room
                .databaseBuilder(context, SubjectsDatabase::class.java, dbName)
                .addMigrations(SubjectMigration1to2())
            if (BuildConfig.DB_ENCRYPTION) {
                builder.openHelperFactory(factory)
            }
            return builder.build()
        }
        const val SUBJECT_DB_VERSION = 2
    }
}
