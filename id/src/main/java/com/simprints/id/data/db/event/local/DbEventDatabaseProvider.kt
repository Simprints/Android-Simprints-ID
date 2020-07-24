package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import net.sqlcipher.database.SQLiteDatabase.getBytes
import net.sqlcipher.database.SupportFactory
import timber.log.Timber

interface DbEventDatabaseFactory {
    fun build(): EventRoomDatabase
}

class DbEventDatabaseFactoryImpl(
    val ctx: Context,
    private val secureLocalDbKeyProvider: SecureLocalDbKeyProvider
) : DbEventDatabaseFactory {

    @OptIn(ExperimentalStdlibApi::class)
    override fun build(): EventRoomDatabase {
        val key = getOrCreateKey(DB_NAME)

        val passphrase: ByteArray = getBytes(key)
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(ctx, EventRoomDatabase::class.java, DB_NAME)
            .openHelperFactory(factory)
            .build()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getOrCreateKey(dbName: String): CharArray {
        return try {
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
        } catch (t: Throwable) {
            Timber.e(t)
            secureLocalDbKeyProvider.setLocalDatabaseKey(dbName)
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)
        }.value.decodeToString().toCharArray()
    }

    companion object {
        private const val DB_NAME = "db_events.room"
    }
}
