package com.simprints.id.data.local

import android.support.test.InstrumentationRegistry
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.RealmConfig
import com.simprints.id.data.db.local.RealmSyncInfo
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.domain.Constants.GROUP.*
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
import java.util.*


open class RealmTestsBase {

    companion object {
        private const val KEY_LENGTH = 64

        const val legacyDatabaseName: String = "legacyDB"
        const val newDatabaseName: String = "newDatabase"
        val newDatabaseKey: ByteArray = Arrays.copyOf("newKey".toByteArray(), KEY_LENGTH)
    }

    protected val localDbKey = LocalDbKey(newDatabaseName, newDatabaseKey, legacyDatabaseName)
    protected val config = RealmConfig.get(localDbKey.projectId, localDbKey.value)

    protected val testContext = InstrumentationRegistry.getTargetContext()
        ?: throw IllegalStateException()

    init {
        deleteRealmFiles(config)
    }

    protected fun getFakePerson(): rl_Person = PeopleGeneratorUtils.getRandomPerson()

    protected fun saveFakePerson(realm: Realm, fakePerson: rl_Person): rl_Person =
        fakePerson.apply { realm.executeTransaction { it.insertOrUpdate(this) } }

    protected fun saveFakePeople(realm: Realm, people: ArrayList<rl_Person>): ArrayList<rl_Person> =
        people.apply { realm.executeTransaction { it.insertOrUpdate(this) } }

    protected fun deleteAll(realm: Realm) = realm.executeTransaction { it.deleteAll() }

    protected fun rl_Person.deepEquals(other: rl_Person): Boolean = when {
        this.patientId != other.patientId -> false
        this.projectId != other.projectId -> false
        this.userId != other.userId -> false
        this.moduleId != other.moduleId -> false
        this.toSync != other.toSync -> false
        this.createdAt != other.createdAt -> false
        this.updatedAt != other.updatedAt -> false
        else -> true
    }

    protected fun saveFakeSyncInfo(realm: Realm,
                                   userId: String = "",
                                   moduleId: String = ""): RealmSyncInfo {
        return when {
            userId.isNotEmpty() -> RealmSyncInfo(USER.ordinal, Date(0))
            moduleId.isNotEmpty() -> RealmSyncInfo(MODULE.ordinal, Date(0))
            else -> RealmSyncInfo(GLOBAL.ordinal, Date(0))
        }.apply { realm.executeTransaction { it.insertOrUpdate(this) } }
    }

    protected fun RealmSyncInfo.deepEquals(other: RealmSyncInfo): Boolean = when {
        this.id != other.id -> false
        this.lastSyncTime != other.lastSyncTime -> false
        else -> true
    }

    protected fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }

}
