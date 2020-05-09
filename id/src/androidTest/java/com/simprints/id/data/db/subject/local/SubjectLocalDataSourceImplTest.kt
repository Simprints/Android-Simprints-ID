package com.simprints.id.data.db.subject.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.SubjectsGeneratorUtils.getRandomPeople
import com.simprints.id.data.db.RealmTestsBase
import com.simprints.id.data.db.subject.domain.FingerprintIdentity
import com.simprints.id.data.db.subject.local.models.DbSubject
import com.simprints.id.data.db.subject.local.models.fromDbToDomain
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.realm.Realm
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SubjectLocalDataSourceImplTest : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var subjectLocalDataSource: SubjectLocalDataSource

    private val loginInfoManagerMock = mock<LoginInfoManager>().apply {
        whenever(this) { getSignedInProjectIdOrEmpty() }
            .thenReturn(DEFAULT_PROJECT_ID)
    }
    private val secureLocalDbKeyProviderMock = mock<SecureLocalDbKeyProvider>().apply {
        whenever(this) { getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) }
            .thenReturn(LocalDbKey(newDatabaseName, newDatabaseKey))
    }

    @Before
    fun setup() {
        realm = Realm.getInstance(config)
        subjectLocalDataSource = SubjectLocalDataSourceImpl(testContext, secureLocalDbKeyProviderMock, loginInfoManagerMock)
    }

    @Test
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = runBlocking { subjectLocalDataSource.count() }
        assertThat(countNewRealm).isEqualTo(1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = "different_newKey".toByteArray().copyOf(KEY_LENGTH)
        val differentSecureLocalDbKeyProviderMock = mock<SecureLocalDbKeyProvider>().apply {
            whenever(this) { getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) }
                .thenReturn(LocalDbKey(differentNewDatabaseName, differentDatabaseKey))
        }
        val differentLocalDataSource = SubjectLocalDataSourceImpl(testContext, differentSecureLocalDbKeyProviderMock, loginInfoManagerMock)

        val count = runBlocking { differentLocalDataSource.count() }
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun givenOneRecordSaved_countShouldReturnOne() = runBlocking {
        saveFakePerson(realm, getFakePerson())

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countShouldReturnMany() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20))

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByUserIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = subjectLocalDataSource.count(SubjectLocalDataSource.Query(userId = fakePerson.attendantId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countByModuleIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = subjectLocalDataSource.count(SubjectLocalDataSource.Query(moduleId = fakePerson.moduleId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countByProjectIdShouldReturnTheRightTotal() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20))

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByPatientIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = subjectLocalDataSource.count(SubjectLocalDataSource.Query(personId = fakePerson.subjectId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun insertOrUpdatePerson_shouldSucceed() = runBlocking {
        val fakePerson = getFakePerson()
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson.fromDbToDomain()))

        realm.executeTransaction {
            assertThat(realm.where(DbSubject::class.java).count()).isEqualTo(1)
            assertThat(realm.where(DbSubject::class.java).findFirst()!!.deepEquals(fakePerson)).isTrue()
        }
    }

    @Test
    fun insertOrUpdateSamePerson_shouldSaveOnlyAPerson() = runBlocking {
        val fakePerson = getFakePerson()
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson.fromDbToDomain()))
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson.fromDbToDomain()))

        realm.executeTransaction {
            assertThat(realm.where(DbSubject::class.java).count()).isEqualTo(1)
            assertThat(realm.where(DbSubject::class.java).findFirst()!!.deepEquals(fakePerson)).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadWithSerializableShouldReturnFingerprintRecords() = runBlocking {
        val fakePerson1 = getFakePerson()
        val fakePerson2 = getFakePerson()
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson1.fromDbToDomain()))
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson2.fromDbToDomain()))

        val fingerprintIdentityLocalDataSource = (subjectLocalDataSource as FingerprintIdentityLocalDataSource)
        val fingerprintIdentities = fingerprintIdentityLocalDataSource.loadFingerprintIdentities(SubjectLocalDataSource.Query()).toList()
        realm.executeTransaction {
            with(fingerprintIdentities) {
                verifyIdentity(fakePerson1, get(0))
                verifyIdentity(fakePerson2, get(1))
            }
        }
    }

    private fun verifyIdentity(subject: DbSubject, fingerprintIdentity: FingerprintIdentity) {
        assertThat(fingerprintIdentity.fingerprints.count()).isEqualTo(subject.fingerprintSamples.count())
        assertThat(fingerprintIdentity.patientId).isEqualTo(subject.subjectId)
    }

    @Test
    fun givenInvalidSerializableQuery_aThrowableIsThrown() {
        runBlocking {
            assertThrows<InvalidQueryToLoadRecordsException> {
                (subjectLocalDataSource as FingerprintIdentityLocalDataSource).loadFingerprintIdentities(mock())
            }
        }
    }


    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runBlocking {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = subjectLocalDataSource.load().toList()

        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = subjectLocalDataSource.load(SubjectLocalDataSource.Query(userId = fakePerson.attendantId)).toList()
        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = subjectLocalDataSource.load(SubjectLocalDataSource.Query(moduleId = fakePerson.moduleId)).toList()
        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByToSyncShouldReturnTheRightPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = subjectLocalDataSource.load(SubjectLocalDataSource.Query(toSync = true)).toList()
        assertThat(people.size).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_loadByToNoSyncShouldReturnTheRightPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = subjectLocalDataSource.load(SubjectLocalDataSource.Query(toSync = false)).toList()
        assertThat(people.size).isEqualTo(0)
    }
}
