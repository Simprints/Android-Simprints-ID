package com.simprints.infra.enrolment.records.room.store

import android.content.Context
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectsDatabaseFactory @Inject constructor(
    @ApplicationContext val ctx: Context,
    private val secureLocalDbKeyProvider: SecurityManager,
) {
    private lateinit var database: SubjectsDatabase

    /**
     * Get the database instance.
     * If the database is not initialized, it will be built.
     */
    fun get(): SubjectsDatabase {
        if (!::database.isInitialized) {
            database = build()
        }
        return database
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun build(): SubjectsDatabase = try {
        val key = getOrCreateKey()
        val passphrase: ByteArray = SQLiteDatabase.getBytes(key)
        val factory = SupportFactory(passphrase)
        SubjectsDatabase.getDatabase(
            ctx,
            factory,
            DB_NAME,
        )
    } catch (t: Throwable) {
        Simber.e("Error creating subject database", t)
        throw t
    }

    private fun getOrCreateKey(): CharArray = try {
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
    } catch (t: Throwable) {
        t.message?.let { Simber.d(it) }
        secureLocalDbKeyProvider.createLocalDatabaseKeyIfMissing(DB_NAME)
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
    }.value.decodeToString().toCharArray()

    companion object {
        private const val DB_NAME = "db-subjects"
    }
}
