package com.simprints.infra.events.event.local

import android.content.Context
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SQLiteDatabase.getBytes
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EventDatabaseFactory @Inject constructor(
    @ApplicationContext val ctx: Context,
    private val securityManager: SecurityManager,
) {
    private lateinit var eventDatabase: EventRoomDatabase

    fun get(): EventRoomDatabase {
        if (!::eventDatabase.isInitialized) {
            build()
        }
        return eventDatabase
    }

    private fun build() {
        try {
            val key = getOrCreateKey(DB_NAME)
            val passphrase: ByteArray = getBytes(key)
            val factory = SupportFactory(passphrase)
            eventDatabase = EventRoomDatabase.getDatabase(
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

    fun recreateDatabase() {
        // DB corruption detected; either DB file or key is corrupt
        // 1. Delete DB file in order to create a new one at next init
        ctx.deleteDatabase(DB_NAME)
        // 2. Recreate the DB key
        securityManager.recreateLocalDatabaseKey(DB_NAME)
        // 3. Rebuild the DB
        build()
    }

    companion object {
        private const val DB_NAME = "dbevents"
    }
}
