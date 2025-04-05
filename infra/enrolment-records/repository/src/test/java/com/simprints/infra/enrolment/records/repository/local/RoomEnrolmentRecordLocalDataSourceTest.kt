package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.models.toDomain
import com.simprints.infra.enrolment.records.repository.local.models.toRealmDb
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class RealmEnrolmentRecordLocalDataSourceTest {
    @MockK
    private lateinit var realm: Realm

    @MockK
    private lateinit var mutableRealm: MutableRealm

    @MockK
    private lateinit var realmWrapperMock: RealmWrapper

    @MockK
    private lateinit var realmQuery: RealmQuery<DbSubject>

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var project: Project

    private lateinit var blockCapture: CapturingSlot<(Realm) -> Any>
    private lateinit var mutableBlockCapture: CapturingSlot<(MutableRealm) -> Any>
    private val onCandidateLoaded: () -> Unit = {}
    private var localSubjects: MutableList<Subject> = mutableListOf()

    private lateinit var enrolmentRecordLocalDataSource: EnrolmentRecordLocalDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        localSubjects = mutableListOf()

        val insertedSubject = slot<DbSubject>()
        every { mutableRealm.delete(any()) } answers { localSubjects.clear() }
        every { mutableRealm.deleteAll() } answers { localSubjects.clear() }
        every { mutableRealm.copyToRealm(capture(insertedSubject), any()) } answers {
            localSubjects.add(insertedSubject.captured.toDomain())
            insertedSubject.captured
        }

        blockCapture = slot()
        coEvery { realmWrapperMock.readRealm(capture(blockCapture)) } answers {
            blockCapture.captured.invoke(realm)
        }
        mutableBlockCapture = slot()
        coEvery { realmWrapperMock.writeRealm(capture(mutableBlockCapture)) } answers {
            mutableBlockCapture.captured.invoke(mutableRealm)
        }
        every { realmQuery.count() } answers {
            mockk { every { find() } returns localSubjects.size.toLong() }
        }

        every { realm.query(DbSubject::class) } returns realmQuery
        every { mutableRealm.query(DbSubject::class) } returns realmQuery

        enrolmentRecordLocalDataSource = RealmEnrolmentRecordLocalDataSource(realmWrapperMock, tokenizationProcessor)
    }

    @Test
    fun givenOneRecordSaved_countShouldReturnOne() = runTest {
        saveFakePerson(getFakePerson())

        val count = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countShouldReturnMany() = runTest {
        saveFakePeople(getRandomPeople(20))

        val count = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByProjectIdShouldReturnTheRightTotal() = runTest {
        saveFakePeople(getRandomPeople(20))

        val count = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenValidSerializableQueryForFingerprints_loadIsCalled() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people = enrolmentRecordLocalDataSource
            .loadFingerprintIdentities(
                SubjectQuery(),
                IntRange(0, 20),
                BiometricDataSource.Simprints,
                project,
                onCandidateLoaded,
            ).toList()

        listOf(fakePerson).zip(people).forEach { (subject, identity) ->
            Truth.assertThat(subject.subjectId).isEqualTo(identity.subjectId)
        }
    }

    @Test
    fun `correctly query supported fingerprint format`() = runTest {
        val format = "SupportedFormat"

        enrolmentRecordLocalDataSource
            .loadFingerprintIdentities(
                SubjectQuery(fingerprintSampleFormat = format),
                IntRange(0, 20),
                BiometricDataSource.Simprints,
                project,
                onCandidateLoaded,
            ).toList()

        verify {
            realmQuery.query(
                "ANY ${EnrolmentRecordLocalDataSource.Companion.FINGERPRINT_SAMPLES_FIELD}.${EnrolmentRecordLocalDataSource.Companion.FORMAT_FIELD} == $0",
                format,
            )
        }
    }

    @Test
    fun `correctly query supported face format`() = runTest {
        val format = "SupportedFormat"

        enrolmentRecordLocalDataSource
            .loadFingerprintIdentities(
                SubjectQuery(faceSampleFormat = format),
                IntRange(0, 20),
                BiometricDataSource.Simprints,
                project,
                onCandidateLoaded,
            ).toList()

        verify {
            realmQuery.query(
                "ANY ${EnrolmentRecordLocalDataSource.Companion.FACE_SAMPLES_FIELD}.${EnrolmentRecordLocalDataSource.Companion.FORMAT_FIELD} == $0",
                format,
            )
        }
    }

    @Test
    fun givenValidSerializableQueryForFace_loadIsCalled() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people = enrolmentRecordLocalDataSource
            .loadFaceIdentities(
                SubjectQuery(),
                IntRange(0, 20),
                BiometricDataSource.Simprints,
                project,
                onCandidateLoaded,
            ).toList()

        listOf(fakePerson).zip(people).forEach { (subject, identity) ->
            Truth.assertThat(subject.subjectId).isEqualTo(identity.subjectId)
        }
    }

    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runTest {
        val fakePerson = getFakePerson()
        saveFakePerson(fakePerson)

        val people = enrolmentRecordLocalDataSource.load(SubjectQuery()).toList()

        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            Truth.assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people =
            enrolmentRecordLocalDataSource
                .load(SubjectQuery(attendantId = savedPersons[0].attendantId))
                .toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            Truth.assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people =
            enrolmentRecordLocalDataSource
                .load(SubjectQuery(moduleId = fakePerson.moduleId.asTokenizableEncrypted()))
                .toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            Truth.assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun performSubjectCreationAction() = runTest {
        val subject = getFakePerson()
        enrolmentRecordLocalDataSource.performActions(
            listOf(SubjectAction.Creation(subject.toDomain())),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectDeletionAction() = runTest {
        val subject = getFakePerson()
        saveFakePerson(subject)
        enrolmentRecordLocalDataSource.performActions(
            listOf(SubjectAction.Deletion(subject.subjectId.toString())),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(peopleCount).isEqualTo(0)
    }

    @Test
    fun performNoAction() = runTest {
        val subject = getFakePerson()
        saveFakePerson(subject)
        enrolmentRecordLocalDataSource.performActions(
            listOf(),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun shouldDeleteAllSubjects() = runTest {
        saveFakePeople(getRandomPeople(5))

        enrolmentRecordLocalDataSource.deleteAll()

        val peopleCount = enrolmentRecordLocalDataSource.count()
        Truth.assertThat(peopleCount).isEqualTo(0)
    }

    private fun getFakePerson(): DbSubject = getRandomSubject().toRealmDb()

    private fun saveFakePerson(fakeSubject: DbSubject): DbSubject = fakeSubject.also { localSubjects.add(it.toDomain()) }

    private fun saveFakePeople(subjects: List<Subject>): List<Subject> = subjects.toMutableList().also { localSubjects.addAll(it) }

    private fun DbSubject.deepEquals(other: DbSubject): Boolean = when {
        this.subjectId != other.subjectId -> false
        this.projectId != other.projectId -> false
        this.attendantId != other.attendantId -> false
        this.moduleId != other.moduleId -> false
        this.createdAt != other.createdAt -> false
        this.updatedAt != other.updatedAt -> false
        else -> true
    }

    private fun getRandomPeople(numberOfPeople: Int): ArrayList<Subject> = arrayListOf<Subject>().also { list ->
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
            FaceSample(Random.Default.nextBytes(64), "faceTemplateFormat"),
            FaceSample(Random.Default.nextBytes(64), "faceTemplateFormat"),
        ),
    ): Subject = Subject(
        subjectId = patientId,
        projectId = projectId,
        attendantId = userId.asTokenizableRaw(),
        moduleId = moduleId.asTokenizableRaw(),
        faceSamples = faceSamples.toList(),
    )
}
