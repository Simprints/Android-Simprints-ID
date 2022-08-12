package com.simprints.id.data.db.subject.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.RealmTestsBase
import com.simprints.id.data.db.subject.domain.FaceIdentity
import com.simprints.id.data.db.subject.domain.FingerprintIdentity
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.db.subject.local.models.DbSubject
import com.simprints.id.data.db.subject.local.models.fromDbToDomain
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import com.simprints.id.testtools.SubjectsGeneratorUtils.getRandomPeople
import com.simprints.infra.login.LoginManager
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
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
    private val loginManagerMock = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }

    private val secureLocalDbKeyProviderMock = mockk<SecurityManager> {
        every { getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) } returns LocalDbKey(
            newDatabaseName,
            newDatabaseKey
        )
    }


    private val testDispatcherProvider = DefaultDispatcherProvider()

    @Before
    fun setup() {
        realm = Realm.getInstance(config)

        subjectLocalDataSource = SubjectLocalDataSourceImpl(
            RealmWrapperImpl(
                testContext,
                secureLocalDbKeyProviderMock,
                loginManagerMock,
                testDispatcherProvider
            )
        )
    }

    @Test
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = runBlocking { subjectLocalDataSource.count() }
        assertThat(countNewRealm).isEqualTo(1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = "different_newKey".toByteArray().copyOf(KEY_LENGTH)

        every { secureLocalDbKeyProviderMock.getLocalDbKeyOrThrow(any()) } returns LocalDbKey(
            differentNewDatabaseName,
            differentDatabaseKey
        )

        val differentLocalDataSource = SubjectLocalDataSourceImpl(
            RealmWrapperImpl(
                testContext,
                secureLocalDbKeyProviderMock,
                loginManagerMock,
                testDispatcherProvider
            )
        )

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
    fun insertOrUpdateSamePerson_shouldSaveOnlyAPerson() = runBlocking {
        val fakePerson = getFakePerson()
        subjectLocalDataSource.performActions(listOf(SubjectAction.Creation(fakePerson.fromDbToDomain())))
        subjectLocalDataSource.performActions(listOf(SubjectAction.Creation(fakePerson.fromDbToDomain())))

        realm.executeTransaction {
            assertThat(realm.where(DbSubject::class.java).count()).isEqualTo(1)
            assertThat(
                realm.where(DbSubject::class.java).findFirst()!!.deepEquals(fakePerson)
            ).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadWithSerializableShouldReturnFingerprintRecords() = runBlocking {
        val fakePerson1 = getFakePerson()
        val fakePerson2 = getFakePerson()
        subjectLocalDataSource.performActions(listOf(SubjectAction.Creation(fakePerson1.fromDbToDomain())))
        subjectLocalDataSource.performActions(listOf(SubjectAction.Creation(fakePerson2.fromDbToDomain())))

        val fingerprintIdentityLocalDataSource =
            (subjectLocalDataSource as FingerprintIdentityLocalDataSource)
        val fingerprintIdentities = fingerprintIdentityLocalDataSource.loadFingerprintIdentities(
            SubjectQuery()
        ).toList()
        realm.executeTransaction {
            with(fingerprintIdentities) {
                verifyIdentity(fakePerson1, find { it.patientId == fakePerson1.subjectId }!!)
                verifyIdentity(fakePerson2, find { it.patientId == fakePerson2.subjectId }!!)
            }
        }
    }

    @Test
    fun givenManyPeopleSaved_loadWithSerializableShouldReturnFaceRecords() = runBlocking {
        val fakePerson1 = getFakePerson()
        val fakePerson2 = getFakePerson()
        subjectLocalDataSource.performActions(listOf(SubjectAction.Creation(fakePerson1.fromDbToDomain())))
        subjectLocalDataSource.performActions(listOf(SubjectAction.Creation(fakePerson2.fromDbToDomain())))

        val faceIdentityDataSource = (subjectLocalDataSource as FaceIdentityLocalDataSource)
        val faceRecords = faceIdentityDataSource.loadFaceIdentities(SubjectQuery()).toList()
        realm.executeTransaction {
            with(faceRecords) {
                verifyIdentity(fakePerson1, find { it.personId == fakePerson1.subjectId }!!)
                verifyIdentity(fakePerson2, find { it.personId == fakePerson2.subjectId }!!)
            }
        }
    }

    @Test
    fun givenInvalidSerializableQuery_aThrowableIsThrown() {
        runBlocking {
            assertThrows<InvalidQueryToLoadRecordsException> {
                (subjectLocalDataSource as FingerprintIdentityLocalDataSource).loadFingerprintIdentities(
                    mockk()
                )
            }
        }
    }


    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runBlocking {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = subjectLocalDataSource.load(SubjectQuery()).toList()

        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people =
            subjectLocalDataSource.load(SubjectQuery(attendantId = fakePerson.attendantId)).toList()
        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people =
            subjectLocalDataSource.load(SubjectQuery(moduleId = fakePerson.moduleId)).toList()
        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
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
