package com.simprints.id.data.local

import android.support.test.InstrumentationRegistry
import com.simprints.id.data.db.ProjectIdProvider
import com.simprints.id.data.db.local.LocalDbKeyProvider
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.local.realm.RealmSyncInfo
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.domain.Constants.GROUP.*
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import io.reactivex.Single
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
    protected val config: RealmConfiguration

    protected val testContext = InstrumentationRegistry.getTargetContext()
        ?: throw IllegalStateException()

    init {
        Realm.init(testContext)
        config = RealmConfig.get(localDbKey.projectId, localDbKey.value)
        deleteRealmFiles(config)
    }

    protected fun getFakePerson(): rl_Person = PeopleGeneratorUtils.getRandomPerson()

    protected fun saveFakePerson(realm: Realm, fakePerson: rl_Person): rl_Person =
        fakePerson.also { realm.executeTransaction { it.insertOrUpdate(fakePerson) } }

    protected fun saveFakePeople(realm: Realm, people: ArrayList<rl_Person>): ArrayList<rl_Person> =
        people.also { realm.executeTransaction { it.insertOrUpdate(people) } }

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
                                   moduleId: String = ""): RealmSyncInfo = when {
        userId.isNotEmpty() -> RealmSyncInfo(USER.ordinal, Date(0))
        moduleId.isNotEmpty() -> RealmSyncInfo(MODULE.ordinal, Date(0))
        else -> RealmSyncInfo(GLOBAL.ordinal, Date(0))
    }.also { info -> realm.executeTransaction { it.insertOrUpdate(info) } }

    protected fun RealmSyncInfo.deepEquals(other: RealmSyncInfo): Boolean =
        syncGroupId == other.syncGroupId && lastSyncTime == other.lastSyncTime

    protected fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }

}

class TestProjectIdProvider : ProjectIdProvider {
    override fun getSignedInProjectId(): Single<String> = Single.create {
        it.onSuccess(RealmTestsBase.newDatabaseName)
    }
}

class TestLocalDbKeyProvider : LocalDbKeyProvider {
    override fun getLocalDbKey(projectId: String): Single<LocalDbKey> = Single.create {
        it.onSuccess(LocalDbKey(RealmTestsBase.newDatabaseName,
            RealmTestsBase.newDatabaseKey,
            RealmTestsBase.legacyDatabaseName))
    }
}
