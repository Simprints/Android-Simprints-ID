package com.simprints.infra.enrolment.records.store.local

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
    @OptIn(ExperimentalStdlibApi::class)
    fun build(): SubjectsDatabase {
        try {
            val key = getOrCreateKey(DB_NAME)
            val passphrase: ByteArray = SQLiteDatabase.getBytes(key)
            // print the key for debugging and will be removed in the final version
            Simber.d("Key: ${passphrase.toHexString()}", "dbkey")
            val factory = SupportFactory(passphrase)
            return SubjectsDatabase.getDatabase(
                ctx,
                factory,
                DB_NAME,
            )
        } catch (t: Throwable) {
            Simber.e("Error creating subject database", t)
            throw t
        }
    }

    private fun getOrCreateKey(
        @Suppress("SameParameterValue") dbName: String,
    ): CharArray = try {
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
    } catch (t: Throwable) {
        t.message?.let { Simber.d(it) }
        secureLocalDbKeyProvider.createLocalDatabaseKeyIfMissing(dbName)
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
    }.value.decodeToString().toCharArray()

    companion object {
        private const val DB_NAME = "db-subjects"
    }
}
