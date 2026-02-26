package com.simprints.infra.protection.database

import android.content.Context
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuxDataDatabaseFactory @Inject constructor(
    @param:ApplicationContext val ctx: Context,
    private val secureLocalDbKeyProvider: SecurityManager,
) {
    private lateinit var auxDataDatabase: AuxDataDatabase

    fun get(): AuxDataDatabase {
        if (!::auxDataDatabase.isInitialized) {
            build()
        }
        return auxDataDatabase
    }

    private fun build() {
        try {
            val key = getOrCreateKey()
            val passphrase: ByteArray = key.toByteArray(Charset.forName("UTF-8"))
            val factory = SupportOpenHelperFactory(passphrase)
            auxDataDatabase = AuxDataDatabase.getDatabase(
                ctx,
                factory,
                DB_NAME,
            )
        } catch (t: Throwable) {
            Simber.e("Failed to create event database", t)
            throw t
        }
    }

    private fun getOrCreateKey(): String = try {
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
    } catch (t: Throwable) {
        t.message?.let { Simber.d(it) }
        secureLocalDbKeyProvider.createLocalDatabaseKeyIfMissing(DB_NAME)
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
    }.value.decodeToString()

    companion object {
        private const val DB_NAME = "subject_aux_data_db"
    }
}
