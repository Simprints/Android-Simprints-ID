package com.simprints.id.data.db.subject.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.SubjectsGeneratorUtils.getRandomPeople
import com.simprints.id.data.db.RealmTestsBase
import com.simprints.id.data.db.subject.domain.FaceIdentity
import com.simprints.id.data.db.subject.domain.FingerprintIdentity
import com.simprints.id.data.db.subject.local.models.DbSubject
import com.simprints.id.data.db.subject.local.models.fromDbToDomain
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.realm.Realm
import kotlinx.coroutines.FlowPreview
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

    private val loginInfoManagerMock = mockk<LoginInfoManager>()
    private val secureLocalDbKeyProviderMock = mockk<SecureLocalDbKeyProvider>()

    @Before
    @FlowPreview
    fun setup() {
        realm = Realm.getInstance(config)
        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { secureLocalDbKeyProviderMock.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) } returns LocalDbKey(newDatabaseName, newDatabaseKey)

        subjectLocalDataSource = SubjectLocalDataSourceImpl(testContext, secureLocalDbKeyProviderMock, loginInfoManagerMock)
    }

    @Test
    @FlowPreview
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = runBlocking { subjectLocalDataSource.count() }
        assertThat(countNewRealm).isEqualTo(1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = "different_newKey".toByteArray().copyOf(KEY_LENGTH)
        val differentSecureLocalDbKeyProviderMock = mockk<SecureLocalDbKeyProvider>()
        every { differentSecureLocalDbKeyProviderMock.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) } returns LocalDbKey(differentNewDatabaseName, differentDatabaseKey)
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

        val count = subjectLocalDataSource.count(SubjectQuery(attendantId = fakePerson.attendantId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countByModuleIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = subjectLocalDataSource.count(SubjectQuery(moduleId = fakePerson.moduleId))
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

        val count = subjectLocalDataSource.count(SubjectQuery(subjectId = fakePerson.subjectId))
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
        val fingerprintIdentities = fingerprintIdentityLocalDataSource.loadFingerprintIdentities(SubjectQuery()).toList()
        realm.executeTransaction {
            with(fingerprintIdentities) {
                verifyIdentity(fakePerson1, get(0))
                verifyIdentity(fakePerson2, get(1))
            }
        }
    }

    @Test
    fun givenManyPeopleSaved_loadWithSerializableShouldReturnFaceRecords() = runBlocking {
        val fakePerson1 = getFakePerson()
        val fakePerson2 = getFakePerson()
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson1.fromDbToDomain()))
        subjectLocalDataSource.insertOrUpdate(listOf(fakePerson2.fromDbToDomain()))

        val faceIdentityDataSource = (subjectLocalDataSource as FaceIdentityLocalDataSource)
        val faceRecords = faceIdentityDataSource.loadFaceIdentities(SubjectQuery()).toList()
        realm.executeTransaction {
            with(faceRecords) {
                verifyIdentity(fakePerson1, get(0))
                verifyIdentity(fakePerson2, get(1))
            }
        }
    }

    @Test
    fun givenInvalidSerializableQuery_aThrowableIsThrown() {
        runBlocking {
            assertThrows<InvalidQueryToLoadRecordsException> {
                (subjectLocalDataSource as FingerprintIdentityLocalDataSource).loadFingerprintIdentities(mockk())
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

        val people = subjectLocalDataSource.load(SubjectQuery(attendantId = fakePerson.attendantId)).toList()
        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = subjectLocalDataSource.load(SubjectQuery(moduleId = fakePerson.moduleId)).toList()
        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByToSyncShouldReturnTheRightPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = subjectLocalDataSource.load(SubjectQuery(toSync = true)).toList()
        assertThat(people.size).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_loadByToNoSyncShouldReturnTheRightPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = subjectLocalDataSource.load(SubjectQuery(toSync = false)).toList()
        assertThat(people.size).isEqualTo(0)
    }

    @Test
    fun shouldDeleteSubject() = runBlocking {
        val subject1 = getFakePerson()
        val subject2 = getFakePerson()
        saveFakePerson(realm, subject1)
        saveFakePerson(realm, subject2)

        subjectLocalDataSource.delete(
            listOf(SubjectQuery(subjectId = subject1.subjectId))
        )

        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun shouldDeleteAllSubjects() = runBlocking {
        saveFakePeople(realm, getRandomPeople(5))

        subjectLocalDataSource.deleteAll()

        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(0)
    }

    private fun verifyIdentity(subject: DbSubject, fingerprintIdentity: FingerprintIdentity) {
        assertThat(fingerprintIdentity.fingerprints.count()).isEqualTo(subject.fingerprintSamples.count())
        assertThat(fingerprintIdentity.patientId).isEqualTo(subject.subjectId)
    }

    private fun verifyIdentity(subject: DbSubject, faceIdentity: FaceIdentity) {
        assertThat(faceIdentity.faces.count()).isEqualTo(subject.faceSamples.count())
        assertThat(faceIdentity.personId).isEqualTo(subject.subjectId)
    }

}
