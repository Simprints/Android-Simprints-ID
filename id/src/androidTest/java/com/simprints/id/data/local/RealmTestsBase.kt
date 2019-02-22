package com.simprints.id.data.local

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.domain.Person
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
import java.util.*

open class RealmTestsBase {

    companion object {
        const val KEY_LENGTH = 64
    }

    val newDatabaseKey: ByteArray = "newKey".toByteArray().copyOf(KEY_LENGTH)
    val legacyDatabaseName: String = "${Date().time}legacyDB"
    val newDatabaseName: String = "${Date().time}newDatabase"

    protected val localDbKey = LocalDbKey(newDatabaseName, newDatabaseKey, legacyDatabaseName)
    protected val config: RealmConfiguration

    protected val testContext = InstrumentationRegistry.getInstrumentation().targetContext
        ?: throw IllegalStateException()

    init {
        Realm.init(testContext)
        config = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        deleteRealmFiles(config)
    }

    protected fun getFakePerson(): rl_Person = PeopleGeneratorUtils.getRandomPerson().toRealmPerson()

    protected fun saveFakePerson(realm: Realm, fakePerson: rl_Person): rl_Person =
        fakePerson.also { realm.executeTransaction { realm -> realm.insertOrUpdate(fakePerson) } }

    protected fun saveFakePeople(realm: Realm, people: List<Person>): List<Person> =
        people.also { realm.executeTransaction { realm -> realm.insertOrUpdate(people.map { person -> person.toRealmPerson() }) } }

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

    protected fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }
}
