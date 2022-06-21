package com.simprints.eventsystem.event.local

import android.content.Context
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.logging.Simber
import net.sqlcipher.database.SQLiteDatabase.getBytes
import net.sqlcipher.database.SupportFactory

interface EventDatabaseFactory {
    fun build(): EventRoomDatabase
}

class DbEventDatabaseFactoryImpl(
    val ctx: Context,
    private val secureLocalDbKeyProvider: SecureLocalDbKeyProvider
) : EventDatabaseFactory {

    override fun build(): EventRoomDatabase {
        try {
            val key = getOrCreateKey(DB_NAME)
            val passphrase: ByteArray = getBytes(key)
            val factory = SupportFactory(passphrase)
            return EventRoomDatabase.getDatabase(
                ctx,
                factory,
                DB_NAME
            )
        } catch (t: Throwable) {
            Simber.e(t)
            throw t
        }
    }

    private fun getOrCreateKey(@Suppress("SameParameterValue") dbName: String): CharArray {
        return try {
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
        } catch (t: Throwable) {
            t.message?.let { Simber.d(it) }
            secureLocalDbKeyProvider.setLocalDatabaseKey(dbName)
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
        }.value.decodeToString().toCharArray()
    }

    companion object {
        private const val DB_NAME = "dbevents"
    }

}
