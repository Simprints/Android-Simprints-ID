package com.simprints.infra.events.event.local

import android.content.Context
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SQLiteDatabase.getBytes
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject

internal class EventDatabaseFactory @Inject constructor(
    @ApplicationContext val ctx: Context,
    private val securityManager: SecurityManager,
) {
    fun build(): EventRoomDatabase {
        try {
            val key = getOrCreateKey(DB_NAME)
            val passphrase: ByteArray = getBytes(key)
            val factory = SupportFactory(passphrase)
            return EventRoomDatabase.getDatabase(
                ctx,
                factory,
                DB_NAME,
            )
        } catch (t: Throwable) {
            Simber.e("Failed to create event database", t)
            throw t
        }
    }

    private fun getOrCreateKey(
        @Suppress("SameParameterValue") dbName: String,
    ): CharArray = try {
        securityManager.getLocalDbKeyOrThrow(dbName)
    } catch (t: Throwable) {
        t.message?.let { Simber.d(it) }
        securityManager.createLocalDatabaseKeyIfMissing(dbName)
        securityManager.getLocalDbKeyOrThrow(dbName)
    }.value.decodeToString().toCharArray()

    fun deleteDatabase() {
        ctx.deleteDatabase(DB_NAME)
    }

    fun recreateDatabaseKey() {
        securityManager.recreateLocalDatabaseKey(DB_NAME)
    }

    companion object {
        private const val DB_NAME = "dbevents"
    }
}
