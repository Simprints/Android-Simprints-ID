package com.simprints.infra.enrolment.records.local

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.exceptions.InvalidQueryToLoadRecordsException
import com.simprints.infra.enrolment.records.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.local.models.fromDomainToDb
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.models.DbSubject
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.random.Random

class SubjectLocalDataSourceImplTest {
    private lateinit var subjectLocalDataSource: SubjectLocalDataSource
    private lateinit var realm: Realm
    private lateinit var realmWrapperMock: RealmWrapper
    private lateinit var blockCapture: CapturingSlot<(Realm) -> Any>
    private var localSubjects: MutableList<Subject> = mutableListOf()

    @Before
    fun setup() {
        localSubjects = mutableListOf()
        realm = mockk {
            val transaction = slot<Realm.Transaction>()
            every { executeTransaction(capture(transaction)) } answers {
                transaction.captured.execute(realm)
            }
            val insertedSubject = slot<DbSubject>()
            every { insertOrUpdate(capture(insertedSubject)) } answers {
                localSubjects.add(insertedSubject.captured.fromDbToDomain())
            }
        }

        realmWrapperMock = mockk()
        subjectLocalDataSource = SubjectLocalDataSourceImpl(realmWrapperMock)
        blockCapture = slot()
        coEvery {
            realmWrapperMock.useRealmInstance(capture(blockCapture))
        } answers { blockCapture.captured.invoke(realm) }


        val realmResults: RealmResults<DbSubject> = mockk(relaxed = true) {
            every { iterator() } answers {
                localSubjects.map { it.fromDomainToDb() }.iterator() as MutableIterator<DbSubject>
            }
            every { deleteAllFromRealm() } answers {
                localSubjects.clear()
                true
            }
        }
        val captureUserId = slot<String>()
        val query: RealmQuery<DbSubject> = mockk(relaxed = true) {
            every {
                equalTo(eq(SubjectLocalDataSourceImpl.USER_ID_FIELD), capture(captureUserId))
            } answers {
                if (localSubjects.none { it.attendantId == captureUserId.captured }) {
                    null
                } else this@mockk
            }
            every { count() } answers {
                localSubjects.size.toLong()
            }
            every { findAll() } answers {

                realmResults
            }
        }
        every { realm.where(DbSubject::class.java) } returns query
    }


    @Test
    fun givenOneRecordSaved_countShouldReturnOne() = runTest {
        saveFakePerson(getFakePerson())

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countShouldReturnMany() = runTest {
        saveFakePeople(getRandomPeople(20))

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByProjectIdShouldReturnTheRightTotal() = runTest {
        saveFakePeople(getRandomPeople(20))

        val count = subjectLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }


    @Test
    fun givenInvalidSerializableQuery_aThrowableIsThrown() {
        runTest {
            assertThrows<InvalidQueryToLoadRecordsException> {
                (subjectLocalDataSource as FingerprintIdentityLocalDataSource).loadFingerprintIdentities(
                    mockk()
                )
            }
        }
    }


    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runTest {
        val fakePerson = getFakePerson()
        saveFakePerson(fakePerson)

        val people = subjectLocalDataSource.load(SubjectQuery()).toList()

        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people =
            subjectLocalDataSource.load(SubjectQuery(attendantId = savedPersons[0].attendantId))
                .toList()
        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people =
            subjectLocalDataSource.load(SubjectQuery(moduleId = fakePerson.moduleId)).toList()
        listOf(fakePerson).zip(people)
            .forEach { assertThat(it.first.deepEquals(it.second.fromDomainToDb())).isTrue() }
    }

    @Test
    fun performSubjectCreationAction() = runTest {
        val subject = getFakePerson()
        subjectLocalDataSource.performActions(
            listOf(SubjectAction.Creation(subject.fromDbToDomain()))
        )
        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectDeletionAction() = runTest {
        val subject = getFakePerson()
        saveFakePerson(subject)
        subjectLocalDataSource.performActions(
            listOf(SubjectAction.Deletion(subject.subjectId.toString()))
        )
        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(0)
    }

    @Test
    fun performNoAction() = runTest {
        val subject = getFakePerson()
        saveFakePerson(subject)
        subjectLocalDataSource.performActions(
            listOf()
        )
        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun shouldDeleteAllSubjects() = runTest {
        saveFakePeople(getRandomPeople(5))

        subjectLocalDataSource.deleteAll()

        val peopleCount = subjectLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(0)
    }

    private fun getFakePerson(): DbSubject =
        getRandomSubject().fromDomainToDb()

    private fun saveFakePerson(fakeSubject: DbSubject): DbSubject =
        fakeSubject.also { localSubjects.add(it.fromDbToDomain()) }

    private fun saveFakePeople(subjects: List<Subject>): List<Subject> =
        subjects.toMutableList().also { localSubjects.addAll(it) }

    private fun DbSubject.deepEquals(other: DbSubject): Boolean = when {
        this.subjectId != other.subjectId -> false
        this.projectId != other.projectId -> false
        this.attendantId != other.attendantId -> false
        this.moduleId != other.moduleId -> false
        this.createdAt != other.createdAt -> false
        this.updatedAt != other.updatedAt -> false
        else -> true
    }

    private fun getRandomPeople(numberOfPeople: Int): ArrayList<Subject> =
        arrayListOf<Subject>().also { list ->
            repeat(numberOfPeople) {
                list.add(getRandomSubject(UUID.randomUUID().toString()))
            }
        }

    private fun getRandomSubject(
        patientId: String = UUID.randomUUID().toString(),
        projectId: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
        moduleId: String = UUID.randomUUID().toString(),
        faceSamples: Array<FaceSample> = arrayOf(
            FaceSample(Random.nextBytes(64), IFaceTemplateFormat.RANK_ONE_1_23),
            FaceSample(Random.nextBytes(64), IFaceTemplateFormat.RANK_ONE_1_23)
        )
    ): Subject =
        Subject(
            subjectId = patientId,
            projectId = projectId,
            attendantId = userId,
            moduleId = moduleId,
            faceSamples = faceSamples.toList()
        )
}
