package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSourceImpl.Companion.FACE_SAMPLES_FIELD
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSourceImpl.Companion.FINGERPRINT_SAMPLES_FIELD
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSourceImpl.Companion.FORMAT_FIELD
import com.simprints.infra.enrolment.records.repository.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.repository.local.models.fromDomainToDb
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
import io.realm.kotlin.query.RealmSingleQuery
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class EnrolmentRecordLocalDataSourceImplTest {
    @MockK
    private lateinit var realm: Realm

    @MockK
    private lateinit var mutableRealm: MutableRealm

    @MockK
    private lateinit var realmWrapperMock: RealmWrapper

    @MockK
    private lateinit var realmQuery: RealmQuery<DbSubject>

    @MockK
    private lateinit var realmSingleQuery: RealmSingleQuery<DbSubject>

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
            localSubjects.add(insertedSubject.captured.fromDbToDomain())
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

        every { realmQuery.query(any(), any()) } returns realmQuery
        every { realmQuery.first() } returns realmSingleQuery

        enrolmentRecordLocalDataSource = EnrolmentRecordLocalDataSourceImpl(
            realmWrapperMock,
            tokenizationProcessor,
        )
    }

    @Test
    fun givenOneRecordSaved_countShouldReturnOne() = runTest {
        saveFakePerson(getFakePerson())

        val count = enrolmentRecordLocalDataSource.count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenManyPeopleSaved_countShouldReturnMany() = runTest {
        saveFakePeople(getRandomPeople(20))

        val count = enrolmentRecordLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenManyPeopleSaved_countByProjectIdShouldReturnTheRightTotal() = runTest {
        saveFakePeople(getRandomPeople(20))

        val count = enrolmentRecordLocalDataSource.count()
        assertThat(count).isEqualTo(20)
    }

    @Test
    fun givenValidSerializableQueryForFingerprints_loadIsCalled() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people = enrolmentRecordLocalDataSource
            .loadFingerprintIdentities(
                SubjectQuery(),
                IntRange(0, 20),
                BiometricDataSource.Simprints,
                project,
                onCandidateLoaded,
            ).toList()

        listOf(fakePerson).zip(people).forEach { (subject, identity) ->
            assertThat(subject.subjectId).isEqualTo(identity.subjectId)
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
                "ANY ${FINGERPRINT_SAMPLES_FIELD}.${FORMAT_FIELD} == $0",
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
                "ANY ${FACE_SAMPLES_FIELD}.${FORMAT_FIELD} == $0",
                format,
            )
        }
    }

    @Test
    fun givenValidSerializableQueryForFace_loadIsCalled() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people = enrolmentRecordLocalDataSource
            .loadFaceIdentities(
                SubjectQuery(),
                IntRange(0, 20),
                BiometricDataSource.Simprints,
                project,
                onCandidateLoaded,
            ).toList()

        listOf(fakePerson).zip(people).forEach { (subject, identity) ->
            assertThat(subject.subjectId).isEqualTo(identity.subjectId)
        }
    }

    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runTest {
        val fakePerson = getFakePerson()
        saveFakePerson(fakePerson)

        val people = enrolmentRecordLocalDataSource.load(SubjectQuery()).toList()

        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.fromDomainToDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people = enrolmentRecordLocalDataSource.load(SubjectQuery(attendantId = savedPersons[0].attendantId)).toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.fromDomainToDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].fromDomainToDb()

        val people = enrolmentRecordLocalDataSource.load(SubjectQuery(moduleId = fakePerson.moduleId.asTokenizableEncrypted())).toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.fromDomainToDb())).isTrue()
        }
    }

    @Test
    fun performSubjectCreationAction() = runTest {
        val subject = getFakePerson()
        every { realmSingleQuery.find() } returns null

        enrolmentRecordLocalDataSource.performActions(
            listOf(SubjectAction.Creation(subject.fromDbToDomain())),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectCreationAction_deletesOldSamples() = runTest {
        every { realmSingleQuery.find() } returns getRandomSubject()
            .copy(
                faceSamples = listOf(
                    getRandomFaceSample("faceToDelete"),
                ),
                fingerprintSamples = listOf(
                    getRandomFingerprintSample("fingerToDelete"),
                ),
            ).fromDomainToDb()
        val subject = getFakePerson()

        enrolmentRecordLocalDataSource.performActions(
            listOf(SubjectAction.Creation(subject.fromDbToDomain())),
            project,
        )

        verify {
            mutableRealm.delete(withArg<DbFaceSample> { it.id == "faceToDelete" })
            mutableRealm.delete(withArg<DbFingerprintSample> { it.id == "faceToDelete" })
        }
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectUpdateAction() = runTest {
        val subject = getFakePerson()
        every { realmSingleQuery.find() } returns getRandomSubject(
            faceSamples = listOf(
                getRandomFaceSample(referenceId = "faceToDelete"),
                getRandomFaceSample(),
            ),
            fingerprintSamples = listOf(
                getRandomFingerprintSample(referenceId = "fingerToDelete"),
                getRandomFingerprintSample(),
            ),
        ).fromDomainToDb()

        enrolmentRecordLocalDataSource.performActions(
            listOf(
                SubjectAction.Update(
                    subject.subjectId.toString(),
                    faceSamplesToAdd = listOf(getRandomFaceSample()),
                    fingerprintSamplesToAdd = listOf(getRandomFingerprintSample()),
                    referenceIdsToRemove = listOf("faceToDelete", "fingerToDelete"),
                ),
            ),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
        verify {
            mutableRealm.delete(withArg<DbFaceSample> { it.id == "faceToDelete" })
            mutableRealm.delete(withArg<DbFingerprintSample> { it.id == "faceToDelete" })
            mutableRealm.copyToRealm(
                withArg<DbSubject> {
                    // one old + one new
                    it.faceSamples.size == 2 &&
                        it.fingerprintSamples.size == 2 &&
                        it.faceSamples.none { it.referenceId == "faceToDelete" } &&
                        it.fingerprintSamples.none { it.referenceId == "fingerToDelete" }
                },
                any(),
            )
        }
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
        assertThat(peopleCount).isEqualTo(0)
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
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun shouldDeleteAllSubjects() = runTest {
        saveFakePeople(getRandomPeople(5))

        enrolmentRecordLocalDataSource.deleteAll()

        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(0)
    }

    private fun getFakePerson(): DbSubject = getRandomSubject().fromDomainToDb()

    private fun saveFakePerson(fakeSubject: DbSubject): DbSubject = fakeSubject.also { localSubjects.add(it.fromDbToDomain()) }

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
        faceSamples: List<FaceSample> = listOf(
            getRandomFaceSample(),
            getRandomFaceSample(),
        ),
        fingerprintSamples: List<FingerprintSample> = listOf(),
    ): Subject = Subject(
        subjectId = patientId,
        projectId = projectId,
        attendantId = userId.asTokenizableRaw(),
        moduleId = moduleId.asTokenizableRaw(),
        faceSamples = faceSamples,
        fingerprintSamples = fingerprintSamples,
    )

    private fun getRandomFaceSample(
        id: String = UUID.randomUUID().toString(),
        referenceId: String = "referenceId",
    ) = FaceSample(Random.nextBytes(64), "faceTemplateFormat", referenceId, id)

    private fun getRandomFingerprintSample(
        id: String = UUID.randomUUID().toString(),
        referenceId: String = "referenceId",
    ) = FingerprintSample(IFingerIdentifier.LEFT_3RD_FINGER, Random.nextBytes(64), 42, "fingerprintTemplateFormat", referenceId, id)
}
