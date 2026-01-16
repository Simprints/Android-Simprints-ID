package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbExternalCredential
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.local.RealmEnrolmentRecordLocalDataSource.Companion.EXTERNAL_CREDENTIAL_FIELD
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class RealmEnrolmentRecordLocalDataSourceTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val OTHER_PROJECT_ID = "otherProjectId"
    }

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
    private var localEnrolmentRecords: MutableList<EnrolmentRecord> = mutableListOf()

    private lateinit var enrolmentRecordLocalDataSource: RealmEnrolmentRecordLocalDataSource

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        localEnrolmentRecords = mutableListOf()

        val insertedSubject = slot<DbSubject>()
        every { mutableRealm.delete(any()) } answers { localEnrolmentRecords.clear() }
        every { mutableRealm.deleteAll() } answers { localEnrolmentRecords.clear() }
        every { mutableRealm.copyToRealm(capture(insertedSubject), any()) } answers {
            localEnrolmentRecords.add(insertedSubject.captured.toDomain())
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
            mockk { every { find() } returns localEnrolmentRecords.size.toLong() }
        }

        every { realm.query(DbSubject::class) } returns realmQuery
        every { mutableRealm.query(DbSubject::class) } returns realmQuery

        every { realmQuery.query(any(), any()) } returns realmQuery
        every { realmQuery.first() } returns realmSingleQuery
        every { realmSingleQuery.find() } returns null

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

        val candidates = mutableListOf<CandidateRecord>()
        enrolmentRecordLocalDataSource
            .loadCandidateRecords(
                EnrolmentRecordQuery(),
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach { candidates.addAll(it.identities) }

        listOf(fakePerson).zip(candidates).forEach { (subject, identity) ->
            assertThat(subject.subjectId).isEqualTo(identity.subjectId)
        }
    }

    @Test
    fun `correctly query supported format`() = runTest {
        val format = "SupportedFormat"

        enrolmentRecordLocalDataSource
            .loadCandidateRecords(
                EnrolmentRecordQuery(format = format),
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach { }
        verify {
            realmQuery.query(
                "ANY ${FINGERPRINT_SAMPLES_FIELD}.${FORMAT_FIELD} == $0 OR ANY $FACE_SAMPLES_FIELD.$FORMAT_FIELD == $1",
                format,
                format,
            )
        }
    }

    @Test
    fun givenValidSerializableQueryForFace_loadIsCalled() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val candidates = mutableListOf<CandidateRecord>()
        enrolmentRecordLocalDataSource
            .loadCandidateRecords(
                EnrolmentRecordQuery(),
                listOf(IntRange(0, 20)),
                BiometricDataSource.Simprints,
                project,
                this,
                onCandidateLoaded,
            ).consumeEach {
                candidates.addAll(it.identities)
            }
        listOf(fakePerson).zip(candidates).forEach { (subject, identity) ->
            assertThat(subject.subjectId).isEqualTo(identity.subjectId)
        }
    }

    @Test
    fun givenManyPeopleSaved_loadShouldReturnThem() = runTest {
        val fakePerson = getFakePerson()
        saveFakePerson(fakePerson)

        val people = enrolmentRecordLocalDataSource.load(EnrolmentRecordQuery()).toList()

        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByUserIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people = enrolmentRecordLocalDataSource.load(EnrolmentRecordQuery(attendantId = savedPersons[0].attendantId)).toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun givenManyPeopleSaved_loadByModuleIdShouldReturnTheRightPeople() = runTest {
        val savedPersons = saveFakePeople(getRandomPeople(20))
        val fakePerson = savedPersons[0].toRealmDb()

        val people = enrolmentRecordLocalDataSource
            .load(
                EnrolmentRecordQuery(moduleId = fakePerson.moduleId.asTokenizableEncrypted()),
            ).toList()
        listOf(fakePerson).zip(people).forEach { (dbSubject, subject) ->
            assertThat(dbSubject.deepEquals(subject.toRealmDb())).isTrue()
        }
    }

    @Test
    fun performSubjectCreationAction() = runTest {
        val subject = getFakePerson()
        every { realmSingleQuery.find() } returns null

        enrolmentRecordLocalDataSource.performActions(
            listOf(EnrolmentRecordAction.Creation(subject.toDomain())),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
    }

    @Test
    fun performSubjectCreationAction_deletesOldSamples() = runTest {
        val faceReferenceId = "faceToDelete"
        val fingerReferenceId = "fingerToDelete"
        every { realmSingleQuery.find() } returns getRandomSubject(
            faceSamples = listOf(
                getRandomFaceReference(referenceId = faceReferenceId),
            ),
            fingerprintSamples = listOf(
                getRandomFingerprintReference(referenceId = fingerReferenceId),
            ),
        ).toRealmDb()

        val subject = getFakePerson()

        enrolmentRecordLocalDataSource.performActions(
            listOf(EnrolmentRecordAction.Creation(subject.toDomain())),
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
                getRandomFaceReference(referenceId = faceReferenceId),
                getRandomFaceReference(),
            ),
            fingerprintSamples = listOf(
                getRandomFingerprintReference(referenceId = fingerReferenceId),
                getRandomFingerprintReference(),
            ),
        ).toRealmDb()

        enrolmentRecordLocalDataSource.performActions(
            listOf(
                EnrolmentRecordAction.Update(
                    subject.subjectId.toString(),
                    samplesToAdd = listOf(getRandomFaceReference(), getRandomFingerprintReference()),
                    referenceIdsToRemove = listOf(faceReferenceId, fingerReferenceId),
                    externalCredentialsToAdd = listOf(),
                    externalCredentialIdsToRemove = listOf(),
                ),
            ),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)

        verify {
            mutableRealm.delete(match<DbFingerprintSample> { it.referenceId == fingerReferenceId })
            mutableRealm.delete(match<DbFaceSample> { it.referenceId == faceReferenceId })
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
    fun performSubjectUpdateExternalActions() = runTest {
        val subject = getFakePerson()
        every { realmSingleQuery.find() } returns getRandomSubject(
            faceSamples = listOf(getRandomFaceReference()),
            fingerprintSamples = listOf(getRandomFingerprintReference()),
            externalCredentials = listOf(
                getRandomExternalCredential("id1"),
                getRandomExternalCredential("id2"),
            ),
        ).toRealmDb()

        enrolmentRecordLocalDataSource.performActions(
            listOf(
                EnrolmentRecordAction.Update(
                    subject.subjectId.toString(),
                    samplesToAdd = listOf(),
                    referenceIdsToRemove = listOf(),
                    externalCredentialsToAdd = listOf(),
                    externalCredentialIdsToRemove = listOf("id1"),
                ),
            ),
            project,
        )
        val peopleCount = enrolmentRecordLocalDataSource.count()
        assertThat(peopleCount).isEqualTo(1)
        verify {
            mutableRealm.delete(match<DbExternalCredential> { it.id == "id1" })
            mutableRealm.copyToRealm(
                match<DbSubject> { subject ->
                    subject.externalCredentials.map { it.id } == listOf("id2")
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
            listOf(EnrolmentRecordAction.Deletion(subject.subjectId.toString())),
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

    @Test
    fun `loads subjects by external credential value`() = runTest {
        val credentialValue = "credentialValue"
        val externalCredential = ExternalCredential(
            id = "id",
            value = credentialValue.asTokenizableEncrypted(),
            subjectId = "subjectId",
            type = ExternalCredentialType.NHISCard,
        )

        saveFakePeople(
            listOf(
                getRandomSubject(externalCredentials = listOf(externalCredential)),
            ),
        )

        enrolmentRecordLocalDataSource.load(
            EnrolmentRecordQuery(externalCredential = credentialValue.asTokenizableEncrypted()),
        )

        verify {
            realmQuery.query(
                "ANY $EXTERNAL_CREDENTIAL_FIELD.value == $0",
                credentialValue.asTokenizableEncrypted().value,
            )
        }
    }

    @Test
    fun `observeCount emits an initial 0 if no records`() = runTest {
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            enrolmentRecordLocalDataSource
                .observeCount(EnrolmentRecordQuery())
                .collect { channel.trySend(it) }
        }

        val firstEmission = channel.receive()
        collectJob.cancel()
        assertThat(firstEmission).isEqualTo(0)
    }

    @Test
    fun `observeCount emits updated count after performActions creation`() = runTest {
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            enrolmentRecordLocalDataSource
                .observeCount(EnrolmentRecordQuery())
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()
        enrolmentRecordLocalDataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(getRandomSubject(projectId = PROJECT_ID))),
            project = project,
        )

        var updated: Int
        do {
            updated = channel.receive()
        } while (updated != 1)
        collectJob.cancel()
        assertThat(initial).isEqualTo(0)
        assertThat(updated).isEqualTo(1)
    }

    @Test
    fun `observeCount emits updated count after delete`() = runTest {
        val createdSubject = getRandomSubject(projectId = PROJECT_ID)
        enrolmentRecordLocalDataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(createdSubject)),
            project = project,
        )
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            enrolmentRecordLocalDataSource
                .observeCount()
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()

        enrolmentRecordLocalDataSource.delete(listOf(EnrolmentRecordQuery(subjectId = createdSubject.subjectId)))

        var afterDelete: Int
        do {
            afterDelete = channel.receive()
        } while (afterDelete != 0)
        collectJob.cancel()
        assertThat(initial).isEqualTo(1)
        assertThat(afterDelete).isEqualTo(0)
    }

    @Test
    fun `observeCount emits updated count after deleteAll`() = runTest {
        enrolmentRecordLocalDataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(getRandomSubject(projectId = PROJECT_ID))),
            project = project,
        )
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            enrolmentRecordLocalDataSource
                .observeCount()
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()

        enrolmentRecordLocalDataSource.deleteAll()

        var afterDelete: Int
        do {
            afterDelete = channel.receive()
        } while (afterDelete != 0)
        collectJob.cancel()
        assertThat(initial).isEqualTo(1)
        assertThat(afterDelete).isEqualTo(0)
    }

    @Test
    fun `observeCount does not include records from other projects`() = runTest {
        val project1RealmQuery = mockk<RealmQuery<DbSubject>>(relaxed = true)
        val project2RealmQuery = mockk<RealmQuery<DbSubject>>(relaxed = true)
        every { realmQuery.query("projectId == $0", PROJECT_ID) } returns project1RealmQuery
        every { realmQuery.query("projectId == $0", OTHER_PROJECT_ID) } returns project2RealmQuery
        every { project1RealmQuery.count() } answers {
            mockk { every { find() } returns localEnrolmentRecords.count { it.projectId == PROJECT_ID }.toLong() }
        }
        every { project2RealmQuery.count() } answers {
            mockk { every { find() } returns localEnrolmentRecords.count { it.projectId == OTHER_PROJECT_ID }.toLong() }
        }
        val project1Channel = Channel<Int>(Channel.UNLIMITED)
        val project2Channel = Channel<Int>(Channel.UNLIMITED)

        val project1CollectJob = launch {
            enrolmentRecordLocalDataSource
                .observeCount(EnrolmentRecordQuery(projectId = PROJECT_ID))
                .collect { project1Channel.trySend(it) }
        }
        val project2CollectJob = launch {
            enrolmentRecordLocalDataSource
                .observeCount(EnrolmentRecordQuery(projectId = OTHER_PROJECT_ID))
                .collect { project2Channel.trySend(it) }
        }

        val project1Initial = project1Channel.receive()
        val project2Initial = project2Channel.receive()

        enrolmentRecordLocalDataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(getRandomSubject(projectId = PROJECT_ID))),
            project = project,
        )

        var project1AfterCreate: Int
        do {
            project1AfterCreate = project1Channel.receive()
        } while (project1AfterCreate != 1)
        advanceUntilIdle()
        val project2AfterInvalidation = project2Channel.tryReceive().getOrNull()
        project1CollectJob.cancel()
        project2CollectJob.cancel()
        assertThat(project1Initial).isEqualTo(0)
        assertThat(project2Initial).isEqualTo(0)
        assertThat(project1AfterCreate).isEqualTo(1)
        assertThat(project2AfterInvalidation).isNull() // same value not re-emitted
    }

    private fun getFakePerson(): DbSubject = getRandomSubject().toRealmDb()

    private fun saveFakePerson(fakeSubject: DbSubject): DbSubject = fakeSubject.also { localEnrolmentRecords.add(it.toDomain()) }

    private fun saveFakePeople(enrolmentRecords: List<EnrolmentRecord>): List<EnrolmentRecord> = enrolmentRecords.toMutableList().also {
        localEnrolmentRecords.addAll(it)
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

    private fun getRandomPeople(numberOfPeople: Int): ArrayList<EnrolmentRecord> = arrayListOf<EnrolmentRecord>().also { list ->
        repeat(numberOfPeople) {
            list.add(getRandomSubject(UUID.randomUUID().toString()))
        }
    }

    private fun getRandomSubject(
        patientId: String = UUID.randomUUID().toString(),
        projectId: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
        moduleId: String = UUID.randomUUID().toString(),
        faceSamples: List<BiometricReference> = listOf(
            getRandomFaceReference(),
            getRandomFaceReference(),
        ),
        fingerprintSamples: List<BiometricReference> = listOf(),
        externalCredentials: List<ExternalCredential> = listOf(
            getRandomExternalCredential(),
        ),
    ): EnrolmentRecord = EnrolmentRecord(
        subjectId = patientId,
        projectId = projectId,
        attendantId = userId.asTokenizableRaw(),
        moduleId = moduleId.asTokenizableRaw(),
        references = fingerprintSamples + faceSamples,
        externalCredentials = externalCredentials,
    )

    private fun getRandomFaceReference(
        id: String = UUID.randomUUID().toString(),
        referenceId: String = "referenceId",
    ) = BiometricReference(
        referenceId = referenceId,
        templates = listOf(
            BiometricTemplate(
                id = id,
                template = Random.nextBytes(64),
            ),
        ),
        format = "faceTemplateFormat",
        modality = Modality.FACE,
    )

    private fun getRandomFingerprintReference(
        id: String = UUID.randomUUID().toString(),
        referenceId: String = "referenceId",
    ) = BiometricReference(
        referenceId = referenceId,
        templates = listOf(
            BiometricTemplate(
                id = id,
                identifier = TemplateIdentifier.LEFT_3RD_FINGER,
                template = Random.nextBytes(64),
            ),
        ),
        format = "fingerprintTemplateFormat",
        modality = Modality.FINGERPRINT,
    )

    private fun getRandomExternalCredential(id: String = "id") = ExternalCredential(
        id = id,
        value = "value".asTokenizableEncrypted(),
        subjectId = "subjectId",
        type = ExternalCredentialType.NHISCard,
    )
}
