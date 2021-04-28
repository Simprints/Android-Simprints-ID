package com.simprints.id.data.db.event.local

import android.content.Context
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import net.sqlcipher.database.SQLiteDatabase.getBytes
import net.sqlcipher.database.SupportFactory
import timber.log.Timber

interface EventDatabaseFactory {
    fun build(): EventRoomDatabase
}

@OptIn(ExperimentalStdlibApi::class)
class DbEventDatabaseFactoryImpl(
    val ctx: Context,
    private val secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
    private val crashReportManager: CrashReportManager
) : EventDatabaseFactory {

    override fun build(): EventRoomDatabase {
        try {
            val key = getOrCreateKey(DB_NAME)
            val passphrase: ByteArray = getBytes(key)
            val factory = SupportFactory(passphrase)
            return EventRoomDatabase.getDatabase(ctx, factory, DB_NAME, crashReportManager)
        } catch (t: Throwable) {
            Timber.e(t)
            throw t
        }
    }

    private fun getOrCreateKey(dbName: String): CharArray {
        return try {
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
        } catch (t: Throwable) {
            Timber.d(t.message)
            secureLocalDbKeyProvider.setLocalDatabaseKey(dbName)
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
        }.value.decodeToString().toCharArray()
    }

    companion object {
        private const val DB_NAME = "dbevents"
    }
}
