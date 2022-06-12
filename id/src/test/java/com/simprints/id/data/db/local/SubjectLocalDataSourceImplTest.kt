package com.simprints.id.data.db.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.commontesttools.SubjectsGeneratorUtils.getRandomPeople
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.*
import com.simprints.id.data.db.subject.local.models.DbSubject
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
open class SubjectLocalDataSourceImplTest {
    private lateinit var subjectLocalDataSource: SubjectLocalDataSource
    private lateinit var realm: Realm
    private lateinit var realmWrapperMock: RealmWrapper
    lateinit var blockCapture: CapturingSlot<(Realm) -> Any>

    companion object {
        const val DEFAULT_PROJECT_ID = "DVXF1mu4CAa5FmiPWHXr"
    }

    @Before
    fun setup() {
        realm = mockk()
        realmWrapperMock = mockk()
        subjectLocalDataSource = SubjectLocalDataSourceImpl(realmWrapperMock)
        blockCapture = slot()
        coEvery {
            realmWrapperMock.useRealmInstance(capture(blockCapture))
        } answers { blockCapture.captured.invoke(realm) }
    }


    @Test
    fun givenOneRecordSaved_countShouldReturnOne() = runBlocking {
        saveFakePerson(getFakePerson())

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countShouldReturnMany() = runBlocking {
        saveFakePeople(getRandomPeople(20))

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByProjectIdShouldReturnTheRightTotal() = runBlocking {
        saveFakePeople(getRandomPeople(20))

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(20)
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
        saveFakePerson(fakePerson)

        val people = subjectLocalDataSource.load(SubjectQuery()).toList()

        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runBlocking {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people =
            subjectLocalDataSource.load(SubjectQuery(attendantId = savedPersons[0].attendantId))
                .toList()
        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runBlocking {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people =
            subjectLocalDataSource.load(SubjectQuery(moduleId = fakePerson.moduleId)).toList()
        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun shouldDeleteSubject() = runBlocking {
        val subject1 = getFakePerson()
        val subject2 = getFakePerson()
        saveFakePerson(subject1)
        saveFakePerson(subject2)

        subjectLocalDataSource.delete(
            listOf(SubjectQuery(subjectId = subject1.subjectId))
        )

        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun shouldDeleteAllSubjects() = runBlocking {
        saveFakePeople(getRandomPeople(5))

        subjectLocalDataSource.deleteAll()

        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(0)
    }


    private fun getFakePerson(): DbSubject =
        SubjectsGeneratorUtils.getRandomSubject().fromDomainToDb()

    private fun saveFakePerson(fakeSubject: DbSubject): DbSubject =
        fakeSubject.also { subject ->
            val query: RealmQuery<DbSubject> = mockk() {
                every {
                    equalTo(SubjectLocalDataSourceImpl.USER_ID_FIELD, subject.attendantId)
                } returns this
                every { findFirst() } returns subject
                every { count() } returns 1
            }
            every { realm.where(DbSubject::class.java) } returns query
        }

    private fun saveFakePeople(subjects: List<Subject>): List<Subject> =
        subjects.toMutableList().also { allSubjects ->
            val results: RealmResults<DbSubject> = mockk() {
                every { iterator() } answers {
                    allSubjects.map { it.fromDomainToDb() }.iterator() as MutableIterator<DbSubject>
                }
                every { deleteAllFromRealm() } answers {
                    allSubjects.clear()
                    true
                }
            }
            val captureUserId = slot<String>()
            val query: RealmQuery<DbSubject> = mockk() {
                every {
                    equalTo(eq(SubjectLocalDataSourceImpl.USER_ID_FIELD), capture(captureUserId))
                } answers {
                    if (subjects.none { it.attendantId == captureUserId.captured }) {
                        null
                    } else this@mockk
                }
                every { count() } answers {
                    allSubjects.size.toLong()
                }
                every { findAll() } returns results
            }
            every { realm.where(DbSubject::class.java) } returns query

        }

    private fun DbSubject.deepEquals(other: DbSubject): Boolean = when {
        this.subjectId != other.subjectId -> false
        this.projectId != other.projectId -> false
        this.attendantId != other.attendantId -> false
        this.moduleId != other.moduleId -> false
        this.createdAt != other.createdAt -> false
        this.updatedAt != other.updatedAt -> false
        else -> true
    }
}
