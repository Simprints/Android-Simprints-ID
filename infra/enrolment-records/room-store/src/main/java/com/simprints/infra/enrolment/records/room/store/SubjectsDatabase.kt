package com.simprints.infra.enrolment.records.room.store

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simprints.infra.enrolment.records.room.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.room.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Singleton
@Database(entities = [DbSubject::class, DbFingerprintSample::class, DbFaceSample::class], version = 1, exportSchema = true)
@Keep
abstract class SubjectsDatabase : RoomDatabase() {
    abstract val subjectDao: SubjectDao

    companion object {
        fun getDatabase(
            context: Context,
            factory: SupportFactory,
            dbName: String,
        ): SubjectsDatabase {
            val builder = Room.databaseBuilder(context, SubjectsDatabase::class.java, dbName)
            if (BuildConfig.DB_ENCRYPTION) {
                builder.openHelperFactory(factory)
            }
            return builder.build()
        }
    }
}
