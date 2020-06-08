package com.simprints.id.data.db.person.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.data.db.RealmTestsBase
import com.simprints.id.data.db.person.domain.FaceIdentity
import com.simprints.id.data.db.person.domain.FingerprintIdentity
import com.simprints.id.data.db.person.local.models.DbPerson
import com.simprints.id.data.db.person.local.models.fromDbToDomain
import com.simprints.id.data.db.person.local.models.fromDomainToDb
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
class PersonLocalDataSourceImplTest : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var personLocalDataSource: PersonLocalDataSource

    private val loginInfoManagerMock = mockk<LoginInfoManager>().apply {
        every { this@apply.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }
    private val secureLocalDbKeyProviderMock = mockk<SecureLocalDbKeyProvider>().apply {
        every { this@apply.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) } returns LocalDbKey(newDatabaseName, newDatabaseKey)
    }

    @Before
    @FlowPreview
    fun setup() {
        realm = Realm.getInstance(config)
        personLocalDataSource = PersonLocalDataSourceImpl(testContext, secureLocalDbKeyProviderMock, loginInfoManagerMock)
    }

    @Test
    @FlowPreview
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = runBlocking { personLocalDataSource.count() }
        assertThat(countNewRealm).isEqualTo(1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = "different_newKey".toByteArray().copyOf(KEY_LENGTH)
        val differentSecureLocalDbKeyProviderMock = mockk<SecureLocalDbKeyProvider>().apply {
            every { this@apply.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) } returns LocalDbKey(differentNewDatabaseName, differentDatabaseKey)
        }
        val differentLocalDataSource = PersonLocalDataSourceImpl(testContext, differentSecureLocalDbKeyProviderMock, loginInfoManagerMock)

        val count = runBlocking { differentLocalDataSource.count() }
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun givenOneRecordSaved_countShouldReturnOne() = runBlocking {
        saveFakePerson(realm, getFakePerson())

        val count = personLocalDataSource.count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countShouldReturnMany() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20))

        val count = personLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByUserIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = personLocalDataSource.count(PersonLocalDataSource.Query(userId = fakePerson.userId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countByModuleIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = personLocalDataSource.count(PersonLocalDataSource.Query(moduleId = fakePerson.moduleId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countByProjectIdShouldReturnTheRightTotal() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20))

        val count = personLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByPatientIdShouldReturnTheRightTotal() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = personLocalDataSource.count(PersonLocalDataSource.Query(personId = fakePerson.patientId))
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun insertOrUpdatePerson_shouldSucceed() = runBlocking {
        val fakePerson = getFakePerson()
        personLocalDataSource.insertOrUpdate(listOf(fakePerson.fromDbToDomain()))

        realm.executeTransaction {
            assertThat(realm.where(DbPerson::class.java).count()).isEqualTo(1)
            assertThat(realm.where(DbPerson::class.java).findFirst()!!.deepEquals(fakePerson)).isTrue()
        }
    }

    @Test
    fun insertOrUpdateSamePerson_shouldSaveOnlyAPerson() = runBlocking {
        val fakePerson = getFakePerson()
        personLocalDataSource.insertOrUpdate(listOf(fakePerson.fromDbToDomain()))
        personLocalDataSource.insertOrUpdate(listOf(fakePerson.fromDbToDomain()))

        realm.executeTransaction {
            assertThat(realm.where(DbPerson::class.java).count()).isEqualTo(1)
            assertThat(realm.where(DbPerson::class.java).findFirst()!!.deepEquals(fakePerson)).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadWithSerializableShouldReturnFingerprintRecords() = runBlocking {
        val fakePerson1 = getFakePerson()
        val fakePerson2 = getFakePerson()
        personLocalDataSource.insertOrUpdate(listOf(fakePerson1.fromDbToDomain()))
        personLocalDataSource.insertOrUpdate(listOf(fakePerson2.fromDbToDomain()))

        val fingerprintIdentityLocalDataSource = (personLocalDataSource as FingerprintIdentityLocalDataSource)
        val fingerprintIdentities = fingerprintIdentityLocalDataSource.loadFingerprintIdentities(PersonLocalDataSource.Query()).toList()
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
        personLocalDataSource.insertOrUpdate(listOf(fakePerson1.fromDbToDomain()))
        personLocalDataSource.insertOrUpdate(listOf(fakePerson2.fromDbToDomain()))

        val faceIdentityDataSource = (personLocalDataSource as FaceIdentityLocalDataSource)
        val faceRecords = faceIdentityDataSource.loadFaceIdentities(PersonLocalDataSource.Query()).toList()
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
                (personLocalDataSource as FingerprintIdentityLocalDataSource).loadFingerprintIdentities(mockk())
            }
        }
    }


    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runBlocking {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = personLocalDataSource.load().toList()

        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = personLocalDataSource.load(PersonLocalDataSource.Query(userId = fakePerson.userId)).toList()
        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runBlocking {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = personLocalDataSource.load(PersonLocalDataSource.Query(moduleId = fakePerson.moduleId)).toList()
        listOf(fakePerson).zip(people).forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByToSyncShouldReturnTheRightPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true)).toList()
        assertThat(people.size).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_loadByToNoSyncShouldReturnTheRightPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = personLocalDataSource.load(PersonLocalDataSource.Query(toSync = false)).toList()
        assertThat(people.size).isEqualTo(0)
    }

    @Test
    fun shouldDeletePerson() = runBlocking {
        val person1 = getFakePerson()
        val person2 = getFakePerson()
        saveFakePerson(realm, person1)
        saveFakePerson(realm, person2)

        personLocalDataSource.delete(
            listOf(PersonLocalDataSource.Query(personId = person1.patientId))
        )

        val peopleCount = personLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun shouldDeleteAllPeople() = runBlocking {
        saveFakePeople(realm, getRandomPeople(5))

        personLocalDataSource.deleteAll()

        val peopleCount = personLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(0)
    }

    private fun verifyIdentity(person: DbPerson, fingerprintIdentity: FingerprintIdentity) {
        assertThat(fingerprintIdentity.fingerprints.count()).isEqualTo(person.fingerprintSamples.count())
        assertThat(fingerprintIdentity.patientId).isEqualTo(person.patientId)
    }

    private fun verifyIdentity(person: DbPerson, faceIdentity: FaceIdentity) {
        assertThat(faceIdentity.faces.count()).isEqualTo(person.faceSamples.count())
        assertThat(faceIdentity.personId).isEqualTo(person.patientId)
    }

}
