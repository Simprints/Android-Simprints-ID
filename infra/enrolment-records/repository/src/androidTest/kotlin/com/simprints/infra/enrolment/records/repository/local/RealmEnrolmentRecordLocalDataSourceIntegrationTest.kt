package com.simprints.infra.enrolment.records.repository.local

import androidx.test.core.app.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.RealmWrapperImpl
import com.simprints.infra.enrolment.records.realm.store.config.RealmConfig
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.IdentityBatch
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.UUID
import kotlin.test.Test

@HiltAndroidTest
class RealmEnrolmentRecordLocalDataSourceIntegrationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    private lateinit var realm: Realm
    private lateinit var realmWrapper: RealmWrapper

    private lateinit var dataSource: RealmEnrolmentRecordLocalDataSource

    private val timeHelper: TimeHelper = mockk {
        every { now() } returns Timestamp(1L)
    }
    private val mockSecurityManager: SecurityManager = mockk {
        justRun { recreateLocalDatabaseKey(any()) }
        every { getLocalDbKeyOrThrow(any()) } returns LocalDbKey("test-project", ByteArray(64) { 1 })
    }
    private val mockAuthStore: AuthStore = mockk {
        every { signedInProjectId } returns "test-project"
        every { signedInUserId } returns "test-user".asTokenizableRaw()
    }
    private val mockTokenizationProcessor: TokenizationProcessor = mockk {
        coEvery {
            tokenizeIfNecessary(any(), any(), any())
        } answers { firstArg() }
    }

    @Before
    fun setUp() {
        hiltRule.inject()

        realmWrapper = RealmWrapperImpl(
            appContext = ApplicationProvider.getApplicationContext(),
            configFactory = RealmConfig(),
            dispatcher = Dispatchers.Unconfined,
            securityManager = mockSecurityManager,
            authStore = mockAuthStore,
        )

        dataSource = RealmEnrolmentRecordLocalDataSource(
            timeHelper = timeHelper,
            realmWrapper = realmWrapper,
            tokenizationProcessor = mockTokenizationProcessor,
            dispatcherIO = Dispatchers.Unconfined,
        )
        // capture the realm instance used by the data source to verify results
        runBlocking {
            realmWrapper.readRealm {
                realm = it
            }
        }
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun givenASubjectCreationActionWhenPerformActionsIsCalledThenTheSubjectIsSavedInRealm() = runTest {
        // Given
        val subjectId = UUID.randomUUID().toString()
        val subject = createTestSubject(subjectId)
        val creationAction = SubjectAction.Creation(subject)
        val project = mockk<Project>()

        // When
        dataSource.performActions(listOf(creationAction), project)

        // Then
        val savedSubject = realm
            .query<DbSubject>(
                "subjectId == $0",
                io.realm.kotlin.types.RealmUUID
                    .from(subjectId),
            ).first()
            .find()
        assertThat(savedSubject).isNotNull()
        assertThat(savedSubject?.subjectId?.toString()).isEqualTo(subjectId)
        assertThat(savedSubject?.projectId).isEqualTo(subject.projectId)
    }

    @Test
    fun givenASubjectExistsWhenLoadIsCalledWithAMatchingQueryThenTheSubjectIsReturned() = runTest {
        // Given
        val subjectId = UUID.randomUUID().toString()
        val subject = createTestSubject(subjectId)
        dataSource.performActions(listOf(SubjectAction.Creation(subject)), mockk())

        val query = SubjectQuery(subjectId = subjectId)

        // When
        val result = dataSource.load(query)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().subjectId).isEqualTo(subjectId)
    }

    @Test
    fun givenASubjectExistsWhenCountIsCalledThenTheCorrectCountIsReturned() = runTest {
        // Given
        val subject1 = createTestSubject()
        val subject2 = createTestSubject(projectId = "other-project")
        dataSource.performActions(
            listOf(
                SubjectAction.Creation(subject1),
                SubjectAction.Creation(subject2),
            ),
            mockk(),
        )
        // Query to match only subject1
        val query = SubjectQuery(projectId = subject1.projectId)

        // When
        val count = dataSource.count(query, mockk())

        // Then
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun givenASubjectDeletionActionWhenPerformActionsIsCalledThenTheSubjectIsRemovedFromRealm() = runTest {
        // Given
        val subjectId = UUID.randomUUID().toString()
        val subject = createTestSubject(subjectId)
        dataSource.performActions(listOf(SubjectAction.Creation(subject)), mockk())

        // Verify it was created
        var count = dataSource.count(SubjectQuery(subjectId = subjectId), mockk())
        assertThat(count).isEqualTo(1)

        val deletionAction = SubjectAction.Deletion(subjectId)

        // When
        dataSource.performActions(listOf(deletionAction), mockk())

        // Then
        count = dataSource.count(SubjectQuery(subjectId = subjectId), mockk())
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun givenMultipleSubjectsExistWhenDeleteIsCalledWithQueriesThenOnlyMatchingSubjectsAreDeleted() = runTest {
        // Given
        val subject1 = createTestSubject(projectId = "proj1")
        val subject2 = createTestSubject(projectId = "proj1")
        val subject3 = createTestSubject(projectId = "proj2")
        dataSource.performActions(
            listOf(
                SubjectAction.Creation(subject1),
                SubjectAction.Creation(subject2),
                SubjectAction.Creation(subject3),
            ),
            mockk(),
        )

        val queryToDelete = SubjectQuery(projectId = "proj1")

        // When
        dataSource.delete(listOf(queryToDelete))

        // Then
        val remainingCount = dataSource.count(SubjectQuery(), mockk())
        assertThat(remainingCount).isEqualTo(1)

        val allSubjects = dataSource.load(SubjectQuery())
        assertThat(allSubjects.first().projectId).isEqualTo("proj2")
    }

    @Test
    fun givenMultipleSubjectsExistWhenDeleteAllIsCalledThenAllSubjectsAreRemoved() = runTest {
        // Given
        val subject1 = createTestSubject()
        val subject2 = createTestSubject()
        dataSource.performActions(
            listOf(
                SubjectAction.Creation(subject1),
                SubjectAction.Creation(subject2),
            ),
            mockk(),
        )

        // When
        dataSource.deleteAll()

        // Then
        val count = dataSource.count(SubjectQuery(), mockk())
        assertThat(count).isEqualTo(0)
    }

    // test subject updates
    @Test
    fun givenASubjectUpdateActionWhenPerformActionsIsCalledThenTheSubjectIsUpdatedInRealm() = runTest {
        // Given
        val subjectId = UUID.randomUUID().toString()
        val originalSubject = createTestSubject(subjectId)
        originalSubject.faceSamples = listOf(
            Sample(
                template = byteArrayOf(),
                format = "ISO",
                referenceId = "ref1",
                modality = Modality.FACE,
            ),
        )
        dataSource.performActions(listOf(SubjectAction.Creation(originalSubject)), mockk())

        val updateAction = SubjectAction.Update(
            subjectId,
            faceSamplesToAdd = listOf(
                Sample(
                    template = byteArrayOf(1, 2, 3),
                    format = "ISO",
                    referenceId = "ref2",
                    modality = Modality.FACE,
                ),
            ),
            fingerprintSamplesToAdd = listOf(
                Sample(
                    template = byteArrayOf(4, 5, 6),
                    format = "ISO",
                    referenceId = "ref3",
                    identifier = SampleIdentifier.LEFT_THUMB,
                    modality = Modality.FINGERPRINT,
                ),
            ),
            referenceIdsToRemove = listOf("ref1"),
            externalCredentialsToAdd = listOf(
                ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        )
        val project = mockk<Project>()
        // When
        dataSource.performActions(listOf(updateAction), project)
        // Then
        val savedSubject = realm
            .query<DbSubject>(
                "subjectId == $0",
                io.realm.kotlin.types.RealmUUID
                    .from(subjectId),
            ).first()
            .find()

        assertThat(savedSubject).isNotNull()
        assertThat(savedSubject?.faceSamples).hasSize(1)
        assertThat(savedSubject?.faceSamples?.first()?.referenceId).isEqualTo("ref2")
        assertThat(savedSubject?.fingerprintSamples).hasSize(1)
        assertThat(savedSubject?.fingerprintSamples?.first()?.referenceId).isEqualTo("ref3")
        savedSubject?.externalCredentials?.first()?.let {
            assertThat(it.id).isEqualTo("id")
            assertThat(it.value).isEqualTo("value")
            assertThat(it.subjectId).isEqualTo("subjectId")
            assertThat(it.type).isEqualTo(ExternalCredentialType.NHISCard.toString())
        }
    }

    @Test
    fun givenManySubjectsWithFaceSamples_whenLoadFaceIdentitiesIsCalledWithRanges_thenReturnsBatchedFaceIdentities() = runTest {
        // Given
        val subjects = (1..10).map { i ->
            createTestSubject(subjectId = UUID.randomUUID().toString()).apply {
                faceSamples = listOf(
                    Sample(
                        template = byteArrayOf(i.toByte()),
                        format = "ISO",
                        referenceId = "ref$i",
                        modality = Modality.FACE,
                    ),
                )
            }
        }
        dataSource.performActions(
            subjects.map { SubjectAction.Creation(it) },
            mockk(),
        )

        val query = SubjectQuery(faceSampleFormat = "ISO")
        val ranges = listOf(0..2, 3..5, 6..9) // 3 batches
        val loadedCandidates = mutableListOf<Unit>()

        // When
        val channel = dataSource.loadFaceIdentities(
            query = query,
            ranges = ranges,
            dataSource = mockk(),
            project = mockk(),
            scope = this,
            onCandidateLoaded = { loadedCandidates.add(Unit) },
        )

        val results = mutableListOf<IdentityBatch<FaceIdentity>>()
        for (batch in channel) {
            results.add(batch)
        }

        // Then
        assertThat(results).hasSize(3)
        assertThat(results[0].identities).hasSize(3)
        assertThat(results[1].identities).hasSize(3)
        assertThat(results[2].identities).hasSize(4)
        assertThat(loadedCandidates).hasSize(10)
    }

    @Test
    fun givenManySubjectsWithFingerprintSamples_whenLoadFingerprintIdentitiesIsCalledWithRanges_thenReturnsBatchedFingerprintIdentities() =
        runTest {
            // Given
            val subjects = (1..10).map { i ->
                createTestSubject(subjectId = UUID.randomUUID().toString()).apply {
                    fingerprintSamples = listOf(
                        Sample(
                            template = byteArrayOf(i.toByte()),
                            format = "ISO",
                            referenceId = "ref$i",
                            identifier = SampleIdentifier.LEFT_THUMB,
                            modality = Modality.FINGERPRINT,
                        ),
                    )
                }
            }
            dataSource.performActions(
                subjects.map { SubjectAction.Creation(it) },
                mockk(),
            )

            val query = SubjectQuery(fingerprintSampleFormat = "ISO")
            val ranges = listOf(0..2, 3..5, 6..9) // 3 batches
            val loadedCandidates = mutableListOf<Unit>()

            // When
            val channel = dataSource.loadFingerprintIdentities(
                query = query,
                ranges = ranges,
                dataSource = mockk(),
                project = mockk(),
                scope = this,
                onCandidateLoaded = { loadedCandidates.add(Unit) },
            )

            val results = mutableListOf<IdentityBatch<FingerprintIdentity>>()
            for (batch in channel) {
                results.add(batch)
            }

            // Then
            assertThat(results).hasSize(3)
            assertThat(results[0].identities).hasSize(3)
            assertThat(results[1].identities).hasSize(3)
            assertThat(results[2].identities).hasSize(4)
            assertThat(loadedCandidates).hasSize(10)
        }

    @Test
    fun givenManySubjects_whenLoadAllSubjectsInBatchesIsCalled_thenReturnsSubjectsInBatches() = runTest {
        // Given
        val subjects = (1..10).map { i ->
            createTestSubject(subjectId = UUID.randomUUID().toString())
        }
        dataSource.performActions(
            subjects.map { SubjectAction.Creation(it) },
            mockk(),
        )

        val batchSize = 4
        val batches = mutableListOf<List<Subject>>()

        // When
        val flow = dataSource.loadAllSubjectsInBatches(batchSize)
        flow.collect { batch ->
            batches.add(batch)
        }

        // Then
        assertThat(batches).hasSize(3)
        assertThat(batches[0]).hasSize(4)
        assertThat(batches[1]).hasSize(4)
        assertThat(batches[2]).hasSize(2)
        assertThat(batches.flatten()).hasSize(10)
    }

    private fun createTestSubject(
        subjectId: String = UUID.randomUUID().toString(),
        projectId: String = "test-project",
        attendantId: String = "test-attendant",
        moduleId: String = "test-module",
    ): Subject = Subject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId.asTokenizableRaw(),
        moduleId = moduleId.asTokenizableRaw(),
    )
}
