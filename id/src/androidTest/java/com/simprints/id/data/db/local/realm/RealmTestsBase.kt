package com.simprints.id.data.db.local.realm

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.person.local.models.DbPerson
import com.simprints.id.data.db.person.local.models.toRealmPerson
import com.simprints.id.data.db.person.domain.Person
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
import java.util.*

open class RealmTestsBase {

    companion object {
        const val KEY_LENGTH = 64
    }

    val newDatabaseKey: ByteArray = "newKey".toByteArray().copyOf(KEY_LENGTH)
    val newDatabaseName: String = "${Date().time}newDatabase"

    private val localDbKey = LocalDbKey(newDatabaseName, newDatabaseKey)
    protected val config: RealmConfiguration

    protected val testContext = InstrumentationRegistry.getInstrumentation().targetContext
        ?: throw IllegalStateException()

    init {
        Realm.init(testContext)
        config = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        deleteRealmFiles(config)
    }

    protected fun getFakePerson(): DbPerson = PeopleGeneratorUtils.getRandomPerson().toRealmPerson()

    protected fun saveFakePerson(realm: Realm, fakePerson: DbPerson): DbPerson =
        fakePerson.also { realm.executeTransaction { realm -> realm.insertOrUpdate(fakePerson) } }

    protected fun saveFakePeople(realm: Realm, people: List<Person>): List<Person> =
        people.also { realm.executeTransaction { realm -> realm.insertOrUpdate(people.map { person -> person.toRealmPerson() }) } }

    protected fun DbPerson.deepEquals(other: DbPerson): Boolean = when {
        this.patientId != other.patientId -> false
        this.projectId != other.projectId -> false
        this.userId != other.userId -> false
        this.moduleId != other.moduleId -> false
        this.toSync != other.toSync -> false
        this.createdAt != other.createdAt -> false
        this.updatedAt != other.updatedAt -> false
        else -> true
    }

    private fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }
}
