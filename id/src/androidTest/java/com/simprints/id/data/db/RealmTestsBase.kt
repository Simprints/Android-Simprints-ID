package com.simprints.id.data.db

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.data.db.common.realm.SubjectsRealmConfig
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.models.DbSubject
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.data.secure.LocalDbKey
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
        config = SubjectsRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        deleteRealmFiles(config)
    }

    protected fun getFakePerson(): DbSubject = SubjectsGeneratorUtils.getRandomSubject().fromDomainToDb()

    protected fun saveFakePerson(realm: Realm, fakeSubject: DbSubject): DbSubject =
        fakeSubject.also { realm.executeTransaction { realm -> realm.insertOrUpdate(fakeSubject) } }

    protected fun saveFakePeople(realm: Realm, subjects: List<Subject>): List<Subject> =
        subjects.also { realm.executeTransaction { realm -> realm.insertOrUpdate(subjects.map { person -> person.fromDomainToDb() }) } }

    protected fun DbSubject.deepEquals(other: DbSubject): Boolean = when {
        this.subjectId != other.subjectId -> false
        this.projectId != other.projectId -> false
        this.attendantId != other.attendantId -> false
        this.moduleId != other.moduleId -> false
        this.createdAt != other.createdAt -> false
        this.updatedAt != other.updatedAt -> false
        else -> true
    }

    private fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }
}
