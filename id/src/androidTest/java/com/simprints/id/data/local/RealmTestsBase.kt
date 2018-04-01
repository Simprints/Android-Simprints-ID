package com.simprints.id.data.local

import android.support.test.InstrumentationRegistry
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.RealmConfig
import com.simprints.id.data.db.local.models.rl_Person
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

    protected fun saveFakePerson(realm: Realm, fakePerson: rl_Person): rl_Person {
        realm.executeTransaction {
            it.insertOrUpdate(fakePerson)
        }
        return fakePerson
    }

    protected fun saveFakePeople(realm: Realm, people: ArrayList<rl_Person>): ArrayList<rl_Person> {
        realm.executeTransaction {
            it.insertOrUpdate(people)
        }
        return people
    }

    protected fun deleteAll(realm: Realm) {
        realm.executeTransaction {
            it.deleteAll()
        }
    }

    protected fun rl_Person.deepEquals(other: rl_Person): Boolean = when {
        this.projectId != other.projectId -> false
        this.userId != other.userId -> false
        this.moduleId != other.moduleId -> false
        else -> true
    }

    protected fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }

}
