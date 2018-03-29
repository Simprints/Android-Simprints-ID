package com.simprints.id.data.local

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.RealmConfig
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.local.models.rl_Fingerprint
import com.simprints.id.data.db.local.models.rl_Person
import io.realm.Realm
import io.realm.RealmList
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
class RealmMigrationTests {

    companion object {
        private const val KEY_LENGTH = 64

        const val legacyDatabaseName: String = "legacyDB"
        val legacyDatabaseKey: ByteArray = Arrays.copyOf(legacyDatabaseName.toByteArray(), KEY_LENGTH)
        const val newDatabaseName: String = "newDatabase"
        val newDatabaseKey: ByteArray = Arrays.copyOf("newKey".toByteArray(), KEY_LENGTH)

        const val FAKE_STRING_FIELD: String = "123"
    }

    private val legacyConfig = RealmConfig.get(legacyDatabaseName, legacyDatabaseKey)
    private val newConfig = RealmConfig.get(newDatabaseName, newDatabaseKey)
    private val localDbKey = LocalDbKey(newDatabaseName, newDatabaseKey, legacyDatabaseName)

    @Test
    fun changeRealmEncryption_ShouldSucceed() {
        Realm.getInstance(legacyConfig).apply { addFakePatient(this); close() }

        RealmDbManager(InstrumentationRegistry.getContext()).apply { signInToLocal(localDbKey) }

        assert(!File(legacyConfig.path).exists())
        assert(File(newConfig.path).exists())
    }

    private fun getFakePatientModel() = rl_Person().apply {
        patientId = FAKE_STRING_FIELD
        userId = FAKE_STRING_FIELD
        moduleId = FAKE_STRING_FIELD
        projectId = FAKE_STRING_FIELD
        @Suppress("RemoveExplicitTypeArguments")
        fingerprints = RealmList<rl_Fingerprint>()
    }

    private fun addFakePatient(realm: Realm) {
        realm.executeTransaction {
            it.insertOrUpdate(getFakePatientModel())
        }
    }
    
}
