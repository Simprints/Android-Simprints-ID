package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth.*
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.RealmEnrolmentRecordLocalDataSource.Companion.FACE_SAMPLES_FIELD
import com.simprints.infra.enrolment.records.repository.local.RealmEnrolmentRecordLocalDataSource.Companion.FINGERPRINT_SAMPLES_FIELD
import com.simprints.infra.enrolment.records.repository.local.RealmEnrolmentRecordLocalDataSource.Companion.FORMAT_FIELD
import com.simprints.infra.enrolment.records.repository.local.models.toDomain
import com.simprints.infra.enrolment.records.repository.local.models.toRealmDb
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmSingleQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class RealmEnrolmentRecordLocalDataSourceTest {
    @MockK
    private lateinit var timeHelper: TimeHelper

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

    private lateinit var blockCapture: CapturingSlot<suspend (Realm) -> Any>
    private lateinit var mutableBlockCapture: CapturingSlot<(MutableRealm) -> Any>
    private val onCandidateLoaded: suspend () -> Unit = {}
    private var localSubjects: MutableList<Subject> = mutableListOf()

    private lateinit var enrolmentRecordLocalDataSource: RealmEnrolmentRecordLocalDataSource

    @OptIn(ExperimentalCoroutinesApi::class)
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
            runBlocking { blockCapture.captured.invoke(realm) }
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

        enrolmentRecordLocalDataSource = RealmEnrolmentRecordLocalDataSource(
            timeHelper,
            realmWrapperMock,
            tokenizationProcessor,
            UnconfinedTestDispatcher(),
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
        val fakePerson = savedPersons[0].toRealmDb()

        val people = mutableListOf<FingerprintIdentity>()
        enrolmentRecordLocalDataSource
            .loadFingerprintIdentities(
                SubjectQuery(),
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach { people.addAll(it.identities) }

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
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach { }

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
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach { }
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
        val fakePerson = savedPersons[0].toRealmDb()

        val people = mutableListOf<FaceIdentity>()
        enrolmentRecordLocalDataSource
            .loadFaceIdentities(
                SubjectQuery(),
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach {
                people.addAll(it.identities)
            }
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
            assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people = enrolmentRecordLocalDataSource.load(SubjectQuery(attendantId = savedPersons[0].attendantId)).toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people = enrolmentRecordLocalDataSource.load(SubjectQuery(moduleId = fakePerson.moduleId.asTokenizableEncrypted())).toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun performSubjectCreationAction() = runTest {
        val subject = getFakePerson()
        every { realmSingleQuery.find() } returns null

        enrolmentRecordLocalDataSource.performActions(
            listOf(SubjectAction.Creation(subject.toDomain())),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectCreationAction_deletesOldSamples() = runTest {
        val faceReferenceId = "faceToDelete"
        val fingerReferenceId = "fingerToDelete"
        every { realmSingleQuery.find() } returns getRandomSubject()
            .copy(
                faceSamples = listOf(
                    getRandomFaceSample(referenceId = faceReferenceId),
                ),
                fingerprintSamples = listOf(
                    getRandomFingerprintSample(referenceId = fingerReferenceId),
                ),
            ).toRealmDb()
        val subject = getFakePerson()

        enrolmentRecordLocalDataSource.performActions(
            listOf(SubjectAction.Creation(subject.toDomain())),
            project,
        )

        verify {
            mutableRealm.delete(match<DbFaceSample> { it.referenceId == faceReferenceId })
            mutableRealm.delete(match<DbFingerprintSample> { it.referenceId == fingerReferenceId })
        }
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectUpdateAction() = runTest {
        val subject = getFakePerson()
        val faceReferenceId = "faceToDelete"
        val fingerReferenceId = "fingerToDelete"
        every { realmSingleQuery.find() } returns getRandomSubject(
            faceSamples = listOf(
                getRandomFaceSample(referenceId = faceReferenceId),
                getRandomFaceSample(),
            ),
            fingerprintSamples = listOf(
                getRandomFingerprintSample(referenceId = fingerReferenceId),
                getRandomFingerprintSample(),
            ),
        ).toRealmDb()

        enrolmentRecordLocalDataSource.performActions(
            listOf(
                SubjectAction.Update(
                    subject.subjectId.toString(),
                    faceSamplesToAdd = listOf(getRandomFaceSample()),
                    fingerprintSamplesToAdd = listOf(getRandomFingerprintSample()),
                    referenceIdsToRemove = listOf(faceReferenceId, fingerReferenceId),
                ),
            ),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
        verify {
            mutableRealm.delete(match<DbFaceSample> { it.referenceId == faceReferenceId })
            mutableRealm.delete(match<DbFingerprintSample> { it.referenceId == fingerReferenceId })
            mutableRealm.copyToRealm(
                match<DbSubject> {
                    // one old + one new
                    it.faceSamples.size == 2 &&
                    it.fingerprintSamples.size == 2 &&
                    it.faceSamples.none { sample -> sample.referenceId == faceReferenceId } &&
                    it.fingerprintSamples.none { sample -> sample.referenceId == fingerReferenceId }
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

    @Test
    fun `loadAllSubjectsInBatches with no subjects should return empty list and close channel`() = runTest {
        val batchSize = 10

        every { realmQuery.sort(RealmEnrolmentRecordLocalDataSource.SUBJECT_ID_FIELD, any()) } returns realmQuery

        val channel = enrolmentRecordLocalDataSource.loadAllSubjectsInBatches(batchSize)
        val result = channel.toList()

        assertThat(result).isEmpty()
    }

    @Test
    fun `getLocalDBInfo returns formatted db info string`() = runTest {
        // Given
        saveFakePeople(getRandomPeople(6))
        every { realm.configuration.name } returns "db-subjects"
        every { realm.configuration.schemaVersion } returns 1L
        // When
        val result = enrolmentRecordLocalDataSource.getLocalDBInfo()
        // Then
        assertThat(result).contains("Database Name: db-subjects")
        assertThat(result).contains("Database Version: 1")
        assertThat(result).contains("Number of Subjects: 6")
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
    ) = FingerprintSample(IFingerIdentifier.LEFT_3RD_FINGER, Random.nextBytes(64), "fingerprintTemplateFormat", referenceId, id)
}
