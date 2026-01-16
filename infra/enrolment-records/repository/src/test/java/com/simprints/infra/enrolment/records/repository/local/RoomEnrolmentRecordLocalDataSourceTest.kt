package com.simprints.infra.enrolment.records.repository.local

import androidx.test.core.app.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource.Simprints
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase.Companion.SUBJECT_DB_VERSION
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabaseFactory
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class RoomEnrolmentRecordLocalDataSourceTest {
    companion object {
        const val PROJECT_1_ID = "project1"
        const val PROJECT_2_ID = "project2"
        val ATTENDANT_1_ID = "attendant1".asTokenizableEncrypted()
        val ATTENDANT_2_ID = "attendant2".asTokenizableEncrypted()
        val MODULE_1_ID = "module1".asTokenizableEncrypted()
        val MODULE_2_ID = "module2".asTokenizableEncrypted()
        val MODULE_3_ID = "module3".asTokenizableEncrypted()

        const val ROC_1_FORMAT = "roc_123"
        const val ROC_3_FORMAT = "roc_3"
        const val NEC_FORMAT = "nec"
        const val ISO_FORMAT = "iso"
        const val UNUSED_FORMAT = "unused"
    }

    // Mocks and setup remain the same
    private lateinit var dataSource: EnrolmentRecordLocalDataSource

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK(relaxed = true)
    lateinit var timeHelper: TimeHelper

    private val subjectDatabaseFactory = SubjectsDatabaseFactory(
        ApplicationProvider.getApplicationContext(),
        mockk {
            every { getLocalDbKeyOrThrow(any()) } returns LocalDbKey("any", byteArrayOf(1, 2, 3))
        },
    )

    // --- Test Data ---
    private val date = Date() // Use a fixed date for consistent timestamps in tests

    // Samples defined first
    private val faceSample1 = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                id = "face-uuid-1",
                template = byteArrayOf(1, 2, 3),
            ),
        ),
        format = ROC_1_FORMAT,
        referenceId = "ref-face-1",
        modality = Modality.FACE,
    )
    private val faceSample2 = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                id = "face-uuid-2",
                template = byteArrayOf(4, 5, 6),
            ),
        ),
        format = ROC_3_FORMAT,
        referenceId = "ref-face-2",
        modality = Modality.FACE,
    )
    private val faceSample3 = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                id = "face-uuid-3-p2",
                template = byteArrayOf(7, 8, 9),
            ),
        ),
        format = ROC_1_FORMAT,
        referenceId = "ref-face-3-p2",
        modality = Modality.FACE,
    )
    private val fingerprintSample1 = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                id = "fp-uuid-1",
                identifier = TemplateIdentifier.LEFT_THUMB,
                template = byteArrayOf(10, 11),
            ),
        ),
        format = NEC_FORMAT,
        referenceId = "ref-fp-1",
        modality = Modality.FINGERPRINT,
    )

    private val fingerprintSample2 = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                id = "fp-uuid-2",
                identifier = TemplateIdentifier.RIGHT_THUMB,
                template = byteArrayOf(12, 13),
            ),
        ),
        format = ISO_FORMAT,
        referenceId = "ref-fp-2",
        modality = Modality.FINGERPRINT,
    )

    private val fingerprintSample3 = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                id = "fp-uuid-3-p2",
                identifier = TemplateIdentifier.LEFT_INDEX_FINGER,
                template = byteArrayOf(14, 15),
            ),
        ),
        format = NEC_FORMAT,
        referenceId = "ref-fp-3-p2",
        modality = Modality.FINGERPRINT,
    )

    // Subjects defined using the samples
    private val enrolmentRecord1P1WithFace = EnrolmentRecord(
        subjectId = "subj-001",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_1_ID,
        references = listOf(faceSample1),
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-001")),
    )
    private val enrolmentRecord2P1WithFinger = EnrolmentRecord(
        subjectId = "subj-002",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_1_ID,
        references = listOf(fingerprintSample1),
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-002")),
    )
    private val enrolmentRecord3P1WithBoth = EnrolmentRecord(
        subjectId = "subj-003",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_2_ID,
        references = listOf(faceSample2, fingerprintSample2),
        externalCredentials = listOf(getExternalCredential("subj-003")),
    )
    private val enrolmentRecord4P2WithBoth = EnrolmentRecord(
        subjectId = "subj-004",
        projectId = PROJECT_2_ID,
        attendantId = ATTENDANT_2_ID,
        moduleId = MODULE_2_ID,
        references = listOf(faceSample3, fingerprintSample3),
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-004")),
    )
    private val enrolmentRecord5P2WithFace = EnrolmentRecord(
        // Added subject
        subjectId = "subj-005",
        projectId = PROJECT_2_ID,
        attendantId = ATTENDANT_2_ID,
        moduleId = MODULE_3_ID,
        references = listOf(faceSample3.copyWithTemplateId(UUID.randomUUID().toString())),
        createdAt = Date(date.time + 1000), // Slightly different time
        updatedAt = Date(date.time + 1000),
        externalCredentials = listOf(getExternalCredential("subj-005")),
    )

    private val enrolmentRecord6P2WithFinger = EnrolmentRecord(
        // Added subject
        subjectId = "subj-006",
        projectId = PROJECT_2_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_3_ID,
        references = listOf(fingerprintSample3.copyWithTemplateId(UUID.randomUUID().toString())),
        createdAt = Date(date.time + 2000), // Different time
        updatedAt = Date(date.time + 2000),
        externalCredentials = listOf(getExternalCredential("subj-006")),
    )
    private val enrolmentRecordInvalidNoSamples = EnrolmentRecord(
        subjectId = "subj-invalid",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_1_ID,
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-invalid")),
    )

    private val project: Project = mockk()
    private lateinit var mockCallback: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        dataSource = RoomEnrolmentRecordLocalDataSource(
            timeHelper,
            subjectDatabaseFactory,
            mockk {
                every { tokenizeIfNecessary(any(), any(), any()) } answers {
                    val arg1 = it.invocation.args[0] as TokenizableString
                    arg1
                }
            },
            queryBuilder = RoomEnrolmentRecordQueryBuilder(),
            dispatcherIO = testCoroutineRule.testCoroutineDispatcher,
        )
        mockCallback = mockk(relaxed = true) // Relaxed mock to avoid specifying return values
        every { project.id } returns PROJECT_1_ID // Basic mock setup for project ID
    }

    @After
    fun tearDown() {
        runBlocking {
            dataSource.deleteAll()
            subjectDatabaseFactory.get().close() // Close the database connection
        }
    }

    private val initialData = listOf(
        EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace),
        EnrolmentRecordAction.Creation(enrolmentRecord2P1WithFinger),
        EnrolmentRecordAction.Creation(enrolmentRecord3P1WithBoth),
        EnrolmentRecordAction.Creation(enrolmentRecord4P2WithBoth),
        EnrolmentRecordAction.Creation(enrolmentRecord5P2WithFace),
        EnrolmentRecordAction.Creation(enrolmentRecord6P2WithFinger),
    )

    private fun getExternalCredential(subjectId: String) = ExternalCredential(
        id = "id",
        value = "value".asTokenizableEncrypted(),
        subjectId = subjectId,
        type = ExternalCredentialType.NHISCard,
    )

    private suspend fun setupInitialData() {
        dataSource.performActions(
            initialData,
            project,
        )
    }

    // --- Test Cases ---

    @Test
    fun `performActions - Creation - should succeed with face sample`() = runTest {
        // Given
        val action = EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)

        // When
        dataSource.performActions(listOf(action), project)

        // Then
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))
        assertThat(loaded).hasSize(1)
        val createdSubject = loaded[0]
        assertThat(createdSubject.subjectId).isEqualTo(enrolmentRecord1P1WithFace.subjectId)
        assertThat(createdSubject.references).hasSize(1)
        assertThat(createdSubject.references).containsExactly(faceSample1)
        assertThat(createdSubject.createdAt).isNotNull()
        assertThat(createdSubject.updatedAt).isNotNull()
    }

    @Test
    fun `performActions - Creation - should succeed with fingerprint sample`() = runTest {
        // Given
        val action = EnrolmentRecordAction.Creation(enrolmentRecord2P1WithFinger)

        // When
        dataSource.performActions(listOf(action), project)

        // Then
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord2P1WithFinger.subjectId))
        assertThat(loaded).hasSize(1)
        val createdSubject = loaded[0]
        assertThat(createdSubject.subjectId).isEqualTo(enrolmentRecord2P1WithFinger.subjectId)
        assertThat(createdSubject.references).hasSize(1)
        assertThat(createdSubject.references).containsExactly(fingerprintSample1)
        assertThat(createdSubject.createdAt).isNotNull()
        assertThat(createdSubject.updatedAt).isNotNull()
    }

    @Test
    fun `performActions - Creation - should succeed with both samples`() = runTest {
        // Given:
        val action = EnrolmentRecordAction.Creation(enrolmentRecord3P1WithBoth)

        // When
        dataSource.performActions(listOf(action), project)

        // Then
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord3P1WithBoth.subjectId))
        assertThat(loaded).hasSize(1)
        val createdSubject = loaded[0]
        assertThat(createdSubject.subjectId).isEqualTo(enrolmentRecord3P1WithBoth.subjectId)
        assertThat(createdSubject.references).hasSize(2)
        assertThat(createdSubject.references).containsExactly(faceSample2, fingerprintSample2)
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `performActions - Creation - should fail without any samples`() = runTest {
        // Given
        val action = EnrolmentRecordAction.Creation(enrolmentRecordInvalidNoSamples) // Subject without samples

        // When
        dataSource.performActions(listOf(action), project) // This line will throw

        // Then: Handled by expected exception.
    }

    @Test
    fun `performActions - Creation - should create multiple valid subjects`() = runTest {
        // Given
        val actions = listOf(
            EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace),
            EnrolmentRecordAction.Creation(enrolmentRecord2P1WithFinger),
            EnrolmentRecordAction.Creation(enrolmentRecord4P2WithBoth), // Belongs to Project 2
        )

        // When
        dataSource.performActions(actions, project)

        // Then
        val loadedAllP1 = dataSource.load(EnrolmentRecordQuery(projectId = PROJECT_1_ID, sort = true))
        val loadedAllP2 = dataSource.load(EnrolmentRecordQuery(projectId = PROJECT_2_ID, sort = true))

        assertThat(loadedAllP1).hasSize(2)
        assertThat(loadedAllP1.map { it.subjectId })
            .containsExactly(
                enrolmentRecord1P1WithFace.subjectId,
                enrolmentRecord2P1WithFinger.subjectId,
            ).inOrder()

        assertThat(loadedAllP2).hasSize(1)
        assertThat(loadedAllP2[0].subjectId).isEqualTo(enrolmentRecord4P2WithBoth.subjectId)
    }

    @Test
    fun `performActions - Update - should add face and fingerprint samples`() = runTest {
        // Given: Create initial subject
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)), project)
        val initialSubject =
            dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId)).first()
        assertThat(initialSubject.references).hasSize(1)

        // Original Update action instantiation style maintained
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord1P1WithFace.subjectId,
            samplesToAdd = listOf(fingerprintSample1, faceSample2),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))
        assertThat(loaded).hasSize(1)
        val updatedSubject = loaded[0]
        assertThat(updatedSubject.references).hasSize(3)
        assertThat(updatedSubject.references).containsExactly(faceSample1, faceSample2, fingerprintSample1)
    }

    @Test
    fun `performActions - Update - should remove face sample when fingerprint sample exists`() = runTest {
        // Given
        val subjectToUpdate = enrolmentRecord3P1WithBoth
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(subjectToUpdate)), project)
        val initial =
            dataSource.load(EnrolmentRecordQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(initial.references).hasSize(2)

        // Original Update action instantiation style maintained
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = subjectToUpdate.subjectId,
            samplesToAdd = listOf(), // Explicitly empty as in original
            referenceIdsToRemove = listOf(faceSample2.referenceId),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded =
            dataSource.load(EnrolmentRecordQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(loaded.references).hasSize(1)
        assertThat(loaded.references).containsExactly(fingerprintSample2)
    }

    @Test
    fun `performActions - Update - should remove fingerprint sample when face sample exists`() = runTest {
        // Given
        val subjectToUpdate = enrolmentRecord3P1WithBoth
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(subjectToUpdate)), project)
        val initial =
            dataSource.load(EnrolmentRecordQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(initial.references).hasSize(2)

        // Original Update action instantiation style maintained
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = subjectToUpdate.subjectId,
            samplesToAdd = listOf(),
            referenceIdsToRemove = listOf(fingerprintSample2.referenceId),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded =
            dataSource.load(EnrolmentRecordQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(loaded.references).hasSize(1)
        assertThat(loaded.references).containsExactly(faceSample2)
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `performActions - Update - should fail when removing last face sample`() = runTest {
        // Given: Subject with only a face sample
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)), project)
        val initial =
            dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId)).first()
        assertThat(initial.references).hasSize(1)

        // Original Update action instantiation style maintained
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord1P1WithFace.subjectId,
            samplesToAdd = listOf(),
            referenceIdsToRemove = listOf(faceSample1.referenceId),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project) // This line will throw

        // Then: Handled by expected exception
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `performActions - Update - should fail when removing last fingerprint sample`() = runTest {
        // Given: Subject with only a fingerprint sample
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(enrolmentRecord2P1WithFinger)), project)
        val initial = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord2P1WithFinger.subjectId)).first()
        assertThat(initial.references).hasSize(1)

        // Original Update action instantiation style maintained
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord2P1WithFinger.subjectId,
            samplesToAdd = listOf(),
            referenceIdsToRemove = listOf(fingerprintSample1.referenceId),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project) // This line will throw

        // Then: Handled by expected exception
    }

    @Test
    fun `performActions - Update - should not add duplicate samples based on ID`() = runTest {
        // Given: Subject already has faceSample1
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)), project)

        // Original Update action instantiation style maintained
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord1P1WithFace.subjectId,
            samplesToAdd = listOf(faceSample1, faceSample2, fingerprintSample1),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))
        assertThat(loaded).hasSize(1)
        val finalSubject = loaded[0]

        assertThat(finalSubject.references).hasSize(3)
        assertThat(finalSubject.references).containsExactly(faceSample1, faceSample2, fingerprintSample1)
    }

    @Test
    fun `performActions - Update - non-existent subjectId - should do nothing`() = runTest {
        // Given
        setupInitialData()
        val initialCount = dataSource.count()
        val nonExistentSubjectId = "subj-does-not-exist"

        val updateAction = EnrolmentRecordAction.Update(
            subjectId = nonExistentSubjectId,
            samplesToAdd = listOf(faceSample1), // Try to add samples
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )

        // When
        // No exception should be thrown here
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val finalCount = dataSource.count()
        assertThat(finalCount).isEqualTo(initialCount) // Count should not change

        // Verify the subject was not created
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = nonExistentSubjectId))
        assertThat(loaded).isEmpty()
    }

    @Test
    fun `load - by no query - should return all subjects`() = runTest {
        // Given
        setupInitialData()

        // When
        val loaded = dataSource.load(EnrolmentRecordQuery())

        // Then
        assertThat(loaded).hasSize(initialData.size)
    }

    @Test
    fun `load - by subjectId - should return correct subject`() = runTest {
        // Given
        setupInitialData()

        // When
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))

        // Then
        assertThat(loaded).hasSize(1)
        assertThat(loaded[0].subjectId).isEqualTo(enrolmentRecord1P1WithFace.subjectId)
        assertThat(loaded[0].references).containsExactly(faceSample1)
    }

    @Test
    fun `load - by hasUntokenizedFields -should return empty list`() = runTest {
        // Given
        setupInitialData()

        // When
        val loaded = dataSource.load(EnrolmentRecordQuery(hasUntokenizedFields = true))

        // Then
        assertThat(loaded).isEmpty()
    }

    @Test
    fun `load - by projectId - should return subjects for that project only`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedP1 = dataSource.load(EnrolmentRecordQuery(projectId = PROJECT_1_ID))
        val loadedP2 = dataSource.load(EnrolmentRecordQuery(projectId = PROJECT_2_ID))

        // Then
        assertThat(loadedP1).hasSize(3)
        val loadedP1Ids = loadedP1.map { it.subjectId }
        assertThat(loadedP1Ids).containsExactly(
            enrolmentRecord1P1WithFace.subjectId,
            enrolmentRecord2P1WithFinger.subjectId,
            enrolmentRecord3P1WithBoth.subjectId,
        )
        assertThat(loadedP2).hasSize(3)
    }

    @Test
    fun `performActions - Deletion - should delete existing subject`() = runTest {
        // Given
        setupInitialData()
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))).isNotEmpty()

        val deleteAction = EnrolmentRecordAction.Deletion(subjectId = enrolmentRecord1P1WithFace.subjectId)

        // When
        dataSource.performActions(listOf(deleteAction), project)

        // Then
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))
        assertThat(loaded).isEmpty()
        assertThat(dataSource.count()).isEqualTo(initialData.size - 1)
    }

    @Test
    fun `combined actions - create, update, delete`() = runTest {
        // --- Create ---
        val createAction = EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)
        dataSource.performActions(listOf(createAction), project)
        var loadedSubject =
            dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId)).firstOrNull()
        assertThat(loadedSubject).isNotNull()
        assertThat(loadedSubject!!.references).hasSize(1)

        // --- Update (Original style maintained) ---
        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord1P1WithFace.subjectId,
            samplesToAdd = listOf(fingerprintSample1),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf(),
        )
        dataSource.performActions(listOf(updateAction), project)
        loadedSubject =
            dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId)).firstOrNull()
        assertThat(loadedSubject).isNotNull()
        assertThat(loadedSubject!!.references).hasSize(2)
        assertThat(loadedSubject.references).containsExactly(fingerprintSample1, faceSample1)

        // --- Delete ---
        val deleteAction = EnrolmentRecordAction.Deletion(subjectId = enrolmentRecord1P1WithFace.subjectId)
        dataSource.performActions(listOf(deleteAction), project)
        val finalLoad = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))
        assertThat(finalLoad).isEmpty()
    }

    @Test
    fun `count - with no query - should return total number of subjects`() = runTest {
        // Given
        setupInitialData()

        // When
        val count = dataSource.count()

        // Then
        assertThat(count).isEqualTo(initialData.size)
    }

    @Test
    fun `count - by projectId - should return count for that project`() = runTest {
        // Given
        setupInitialData()

        // When
        val countP1 = dataSource.count(EnrolmentRecordQuery(projectId = PROJECT_1_ID))
        val countP2 = dataSource.count(EnrolmentRecordQuery(projectId = PROJECT_2_ID))

        // Then
        assertThat(countP1).isEqualTo(3)
        assertThat(countP2).isEqualTo(3)
    }

    @Test
    fun `count - by attendantId - should return count for that attendant`() = runTest {
        // Given
        setupInitialData()

        // When
        val countAtt1 = dataSource.count(EnrolmentRecordQuery(attendantId = ATTENDANT_1_ID))
        val countAtt2 = dataSource.count(EnrolmentRecordQuery(attendantId = ATTENDANT_2_ID))

        // Then
        assertThat(countAtt1).isEqualTo(4)
        assertThat(countAtt2).isEqualTo(2)
    }

    @Test
    fun `count - by moduleId - should return count for that module`() = runTest {
        // Given
        setupInitialData()

        // When
        val countModule1 = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_1_ID))
        val countModule2 = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_2_ID))

        // Then
        assertThat(countModule1).isEqualTo(2)
        assertThat(countModule2).isEqualTo(2)
    }

    @Test
    fun `count - by faceSampleFormat - should return count of subjects with matching format`() = runTest {
        // Given
        setupInitialData()

        // When
        val countRoc1P1 = dataSource.count(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                format = ROC_1_FORMAT,
            ),
        )
        val countRoc3P1 = dataSource.count(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                format = ROC_3_FORMAT,
            ),
        )
        val countRoc1P2 = dataSource.count(
            EnrolmentRecordQuery(
                projectId = PROJECT_2_ID,
                format = ROC_1_FORMAT,
            ),
        )
        val countAllRoc1 = dataSource.count(EnrolmentRecordQuery(format = ROC_1_FORMAT))

        // Then
        assertThat(countRoc1P1).isEqualTo(1)
        assertThat(countRoc3P1).isEqualTo(1)
        assertThat(countRoc1P2).isEqualTo(2)
        assertThat(countAllRoc1).isEqualTo(3)
    }

    @Test
    fun `count - by fingerprintSampleFormat - should return count of subjects with matching format`() = runTest {
        // Given
        setupInitialData()

        // When
        val countNecP1 = dataSource.count(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                format = NEC_FORMAT,
            ),
        )
        val countIsoP1 = dataSource.count(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                format = ISO_FORMAT,
            ),
        )
        val countNecP2 = dataSource.count(
            EnrolmentRecordQuery(
                projectId = PROJECT_2_ID,
                format = NEC_FORMAT,
            ),
        )
        val countAllNec = dataSource.count(EnrolmentRecordQuery(format = NEC_FORMAT))

        // Then
        assertThat(countNecP1).isEqualTo(1)
        assertThat(countIsoP1).isEqualTo(1)
        assertThat(countNecP2).isEqualTo(2)
        assertThat(countAllNec).isEqualTo(3)
    }

    @Test
    fun `count - with query matching nothing - should return zero`() = runTest {
        // Given
        setupInitialData()

        // When
        val count = dataSource.count(
            EnrolmentRecordQuery(
                projectId = "non-existent-project",
                moduleId = "non-existent-module".asTokenizableEncrypted(),
                attendantId = "non-existent-attendant".asTokenizableEncrypted(),
            ),
        )

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `observeCount emits an initial 0 if no records`() = runTest {
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            dataSource.observeCount(EnrolmentRecordQuery(projectId = PROJECT_1_ID))
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
            dataSource.observeCount()
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()
        dataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)),
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
        val createdSubject = enrolmentRecord1P1WithFace
        dataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(createdSubject)),
            project = project,
        )
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            dataSource.observeCount()
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()

        dataSource.delete(listOf(EnrolmentRecordQuery(subjectId = createdSubject.subjectId)))

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
        dataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)),
            project = project,
        )
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            dataSource.observeCount()
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()

        dataSource.deleteAll()

        var afterDelete: Int
        do {
            afterDelete = channel.receive()
        } while (afterDelete != 0)
        collectJob.cancel()
        assertThat(initial).isEqualTo(1)
        assertThat(afterDelete).isEqualTo(0)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `observeCount does not include records from other projects`() = runTest {
        val project1Channel = Channel<Int>(Channel.UNLIMITED)
        val project2Channel = Channel<Int>(Channel.UNLIMITED)

        val project1CollectJob = launch {
            dataSource.observeCount(EnrolmentRecordQuery(projectId = PROJECT_1_ID))
                .collect { project1Channel.trySend(it) }
        }
        val project2CollectJob = launch {
            dataSource.observeCount(EnrolmentRecordQuery(projectId = PROJECT_2_ID))
                .collect { project2Channel.trySend(it) }
        }

        val project1Initial = project1Channel.receive()
        val project2Initial = project2Channel.receive()

        dataSource.performActions(
            actions = listOf(EnrolmentRecordAction.Creation(enrolmentRecord1P1WithFace)),
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

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `loadIdentities - should throw exception if format is missing in query`() = runTest {
        // Given
        setupInitialData()
        val queryWithoutFormat = EnrolmentRecordQuery(projectId = PROJECT_1_ID)

        // When
        dataSource.loadCandidateRecords(
            // This call will throw
            query = queryWithoutFormat,
            ranges = listOf(0..10),
            project = project,
            dataSource = Simprints,
            scope = this,
            onCandidateLoaded = mockCallback,
        )

        // Then: Handled by expected exception
        // Callback should not be called if exception occurs before iteration
        verify(exactly = 0) { mockCallback() }
    }

    @Test
    fun `loadIdentities - should load identities matching fingerprint format for the specified project`() = runTest {
        // Given
        setupInitialData()
        val project2Mock: Project = mockk { every { id } returns PROJECT_2_ID }

        // When - Query P1 for NEC
        val loadedP1Nec = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(
                    projectId = PROJECT_1_ID,
                    format = NEC_FORMAT,
                ),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        // When - Query P1 for ISO
        val loadedP1Iso = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(
                    projectId = PROJECT_1_ID,
                    format = ISO_FORMAT,
                ),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        // When - Query P2 for NEC
        val loadedP2Nec = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(
                    projectId = PROJECT_2_ID,
                    format = NEC_FORMAT,
                ),
                ranges = listOf(0..10),
                project = project2Mock,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities

        // Then - P1 NEC
        assertThat(loadedP1Nec).hasSize(1)
        assertThat(loadedP1Nec[0].subjectId).isEqualTo(enrolmentRecord2P1WithFinger.subjectId)
        assertThat(loadedP1Nec[0].references).hasSize(1)
        assertThat(loadedP1Nec[0].references[0].format).isEqualTo(NEC_FORMAT)
        assertThat(loadedP1Nec[0].references).isEqualTo(enrolmentRecord2P1WithFinger.references)

        // Then - P1 ISO
        assertThat(loadedP1Iso).hasSize(1)
        assertThat(loadedP1Iso[0].subjectId).isEqualTo(enrolmentRecord3P1WithBoth.subjectId)
        assertThat(loadedP1Iso[0].references).hasSize(1)
        assertThat(loadedP1Iso[0].references[0].format).isEqualTo(ISO_FORMAT)
        assertThat(
            loadedP1Iso[0].references,
        ).isEqualTo(enrolmentRecord3P1WithBoth.references.filter { it.modality == Modality.FINGERPRINT })

        // Then - P2 NEC
        assertThat(loadedP2Nec).hasSize(2)
        assertThat(loadedP2Nec[0].subjectId).isEqualTo(enrolmentRecord4P2WithBoth.subjectId)
        assertThat(loadedP2Nec[0].references).hasSize(1)
        assertThat(loadedP2Nec[0].references[0].format).isEqualTo(NEC_FORMAT)

        verify(exactly = 4) { mockCallback() }
    }

    @Test
    fun `loadIdentities - should respect the range parameter for specific fingerprint format`() = runTest {
        // Given: More data
        val subject5P1WithNec = enrolmentRecord2P1WithFinger.copy(
            subjectId = "subj-005",
            references = listOf(fingerprintSample1.copyWithTemplateId("fp-uuid-5").copy(referenceId = "ref-fp-5")),
            createdAt = Date(date.time + 1000),
        )
        val subject6P1WithNec = enrolmentRecord2P1WithFinger.copy(
            subjectId = "subj-006",
            references = listOf(fingerprintSample1.copyWithTemplateId("fp-uuid-6").copy(referenceId = "ref-fp-6")),
            createdAt = Date(date.time + 2000),
        )
        setupInitialData()
        dataSource.performActions(
            listOf(
                EnrolmentRecordAction.Creation(subject5P1WithNec),
                EnrolmentRecordAction.Creation(subject6P1WithNec),
            ),
            project,
        )
        val baseQuery = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            format = NEC_FORMAT,
        )
        // When
        val loadedRanges =
            dataSource
                .loadCandidateRecords(
                    query = baseQuery,
                    ranges = listOf(
                        0..0,
                        1..1,
                    ),
                    project = project,
                    dataSource = Simprints,
                    scope = this,
                    onCandidateLoaded = mockCallback,
                ).toList()

        val loadedFirstTwo =
            dataSource
                .loadCandidateRecords(
                    query = baseQuery,
                    ranges = listOf(
                        0..1,
                    ),
                    project = project,
                    dataSource = Simprints,
                    scope = this,
                    onCandidateLoaded = mockCallback,
                ).toList()
                .first()
                .identities
        val loadedAll =
            dataSource
                .loadCandidateRecords(
                    query = baseQuery,
                    ranges = listOf(0..10),
                    project = project,
                    dataSource = Simprints,
                    scope = this,
                    onCandidateLoaded = mockCallback,
                ).toList()
                .first()
                .identities

        // Then
        assertThat(loadedRanges).hasSize(2)
        assertThat(loadedRanges[0].identities[0].subjectId).isEqualTo(enrolmentRecord2P1WithFinger.subjectId)
        assertThat(loadedRanges[1].identities).hasSize(1)
        assertThat(loadedRanges[1].identities[0].subjectId).isEqualTo(subject5P1WithNec.subjectId)
        assertThat(loadedFirstTwo).hasSize(2)
        assertThat(loadedFirstTwo.map { it.subjectId })
            .containsExactly(enrolmentRecord2P1WithFinger.subjectId, subject5P1WithNec.subjectId)
            .inOrder()
        assertThat(loadedAll).hasSize(3)
        assertThat(loadedAll.map { it.subjectId })
            .containsExactly(
                enrolmentRecord2P1WithFinger.subjectId,
                subject5P1WithNec.subjectId,
                subject6P1WithNec.subjectId,
            ).inOrder()
        verify(exactly = 7) { mockCallback() }
    }

    @Test
    fun `loadIdentities - with query format matching nothing - should return empty list`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedIdentities = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(
                    projectId = PROJECT_1_ID,
                    format = UNUSED_FORMAT,
                ),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities

        // Then
        assertThat(loadedIdentities).isEmpty()
        verify(exactly = 0) { mockCallback() }
    }

    @Test
    fun `loadIdentities - should load identities matching face format for the specified project`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedP1Roc1 = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(projectId = PROJECT_1_ID, format = ROC_1_FORMAT),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1Roc3 = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(projectId = PROJECT_1_ID, format = ROC_3_FORMAT),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP2Roc1 = dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(projectId = PROJECT_2_ID, format = ROC_1_FORMAT),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities

        // Then - P1 ROC_1
        assertThat(loadedP1Roc1).hasSize(1)
        assertThat(loadedP1Roc1[0].subjectId).isEqualTo(enrolmentRecord1P1WithFace.subjectId)
        assertThat(loadedP1Roc1[0].references).hasSize(1)
        assertThat(loadedP1Roc1[0].references[0].format).isEqualTo(ROC_1_FORMAT)
        assertThat(loadedP1Roc1[0].references).isEqualTo(enrolmentRecord1P1WithFace.references)

        // Then - P1 ROC_3
        assertThat(loadedP1Roc3).hasSize(1)
        assertThat(loadedP1Roc3[0].subjectId).isEqualTo(enrolmentRecord3P1WithBoth.subjectId)
        assertThat(loadedP1Roc3[0].references).hasSize(1)
        assertThat(loadedP1Roc3[0].references[0].format).isEqualTo(ROC_3_FORMAT)
        assertThat(loadedP1Roc3[0].references).isEqualTo(enrolmentRecord3P1WithBoth.references.filter { it.modality == Modality.FACE })

        // Then - P2 ROC_1
        assertThat(loadedP2Roc1).hasSize(2)
        assertThat(loadedP2Roc1[0].subjectId).isEqualTo(enrolmentRecord4P2WithBoth.subjectId)

        verify(exactly = 4) { mockCallback() }
    }

    @Test
    fun `loadIdentities - should respect the range parameter for specific face format`() = runTest {
        // Given: More data
        val subject5P1WithRoc1 = enrolmentRecord1P1WithFace.copy(
            subjectId = "subj-005",
            references = listOf(faceSample1.copyWithTemplateId("face-uuid-5").copy(referenceId = "ref-face-5")),
            createdAt = Date(date.time + 1000),
        )
        val subject6P1WithRoc1 = enrolmentRecord1P1WithFace.copy(
            subjectId = "subj-006",
            references = listOf(faceSample1.copyWithTemplateId("face-uuid-6").copy(referenceId = "ref-face-6")),
            createdAt = Date(date.time + 2000),
        )
        setupInitialData()
        dataSource.performActions(
            listOf(
                EnrolmentRecordAction.Creation(subject5P1WithRoc1),
                EnrolmentRecordAction.Creation(subject6P1WithRoc1),
            ),
            project,
        )
        val baseQuery =
            EnrolmentRecordQuery(projectId = PROJECT_1_ID, format = ROC_1_FORMAT, sort = true)
        // When
        val loadedRanges =
            dataSource
                .loadCandidateRecords(
                    query = baseQuery,
                    ranges = listOf(
                        0..0,
                        1..1,
                    ),
                    project = project,
                    dataSource = Simprints,
                    scope = this,
                    onCandidateLoaded = mockCallback,
                ).toList()

        val loadedFirstTwo =
            dataSource
                .loadCandidateRecords(
                    query = baseQuery,
                    ranges = listOf(
                        0..1,
                    ),
                    project = project,
                    dataSource = Simprints,
                    scope = this,
                    onCandidateLoaded = mockCallback,
                ).toList()
                .first()
                .identities
        val loadedAll = dataSource
            .loadCandidateRecords(
                query = baseQuery,
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities

        // Then
        assertThat(loadedRanges).hasSize(2) // Two ranges loaded
        assertThat(loadedRanges[0].identities[0].subjectId).isEqualTo(enrolmentRecord1P1WithFace.subjectId)
        assertThat(loadedRanges[1].identities).hasSize(1)
        assertThat(loadedRanges[1].identities[0].subjectId).isEqualTo(subject5P1WithRoc1.subjectId)
        assertThat(loadedFirstTwo).hasSize(2)
        assertThat(loadedFirstTwo.map { it.subjectId })
            .containsExactly(enrolmentRecord1P1WithFace.subjectId, subject5P1WithRoc1.subjectId)
            .inOrder()
        assertThat(loadedAll).hasSize(3)
        assertThat(loadedAll.map { it.subjectId })
            .containsExactly(
                enrolmentRecord1P1WithFace.subjectId,
                subject5P1WithRoc1.subjectId,
                subject6P1WithRoc1.subjectId,
            ).inOrder()
        verify(exactly = 7) { mockCallback() }
    }

    @Test
    fun `loadIdentities - by attendantId and moduleId and face format - should return correct identities`() = runTest {
        // Given
        setupInitialData()

        // Query for Project 1, Attendant 1, Module 1, Format ROC_1
        val queryP1A1M1Roc1 = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_1_ID,
            format = ROC_1_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 2, Format ROC_3
        val queryP1A1M2Roc3 = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_2_ID,
            format = ROC_3_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 1, Format ROC_3 (should be empty)
        val queryP1A1M1Roc3Empty = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_1_ID,
            format = ROC_3_FORMAT,
        )

        // When
        val loadedP1A1M1Roc1 = dataSource
            .loadCandidateRecords(
                queryP1A1M1Roc1,
                listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1A1M2Roc3 = dataSource
            .loadCandidateRecords(
                queryP1A1M2Roc3,
                listOf(
                    0..10,
                ),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1A1M1Roc3Empty =
            dataSource
                .loadCandidateRecords(
                    queryP1A1M1Roc3Empty,
                    listOf(
                        0..10,
                    ),
                    project = project,
                    dataSource = Simprints,
                    scope = this,
                    onCandidateLoaded = mockCallback,
                ).toList()
                .first()
                .identities

        // Then
        assertThat(loadedP1A1M1Roc1).hasSize(1)
        assertThat(loadedP1A1M1Roc1[0].subjectId).isEqualTo(enrolmentRecord1P1WithFace.subjectId)

        assertThat(loadedP1A1M2Roc3).hasSize(1)
        assertThat(loadedP1A1M2Roc3[0].subjectId).isEqualTo(enrolmentRecord3P1WithBoth.subjectId)

        assertThat(loadedP1A1M1Roc3Empty).isEmpty()

        verify(exactly = 2) { mockCallback() } // Called for the 3 successful loads
    }

    @Test
    fun `loadIdentities - by attendantId and moduleId and fingerprint format - should return correct identities`() = runTest {
        // Given
        setupInitialData()

        // Query for Project 1, Attendant 1, Module 1, Format NEC
        val queryP1A1M1Nec = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_1_ID,
            format = NEC_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 3, Format NEC
        val queryP1A1M3Nec = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_3_ID,
            format = NEC_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 2, Format ISO
        val queryP1A1M2Iso = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_2_ID,
            format = ISO_FORMAT,
        )

        // Query for Project 1, Attendant 2, Module 1, Format NEC (should be empty)
        val queryP1A2M1NecEmpty = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_2_ID,
            moduleId = MODULE_1_ID,
            format = NEC_FORMAT,
        )

        // When
        val loadedP1A1M1Nec = dataSource
            .loadCandidateRecords(
                queryP1A1M1Nec,
                listOf(
                    0..10,
                ),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1A1M3Nec = dataSource
            .loadCandidateRecords(
                queryP1A1M3Nec,
                listOf(
                    0..10,
                ),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1A1M2Iso = dataSource
            .loadCandidateRecords(
                queryP1A1M2Iso,
                listOf(
                    0..10,
                ),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1A2M1NecEmpty = dataSource
            .loadCandidateRecords(
                queryP1A2M1NecEmpty,
                listOf(
                    0..10,
                ),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities

        // Then
        assertThat(loadedP1A1M1Nec).hasSize(1)
        assertThat(loadedP1A1M1Nec[0].subjectId).isEqualTo(enrolmentRecord2P1WithFinger.subjectId)

        assertThat(loadedP1A1M3Nec).hasSize(0)

        assertThat(loadedP1A1M2Iso).hasSize(1)
        assertThat(loadedP1A1M2Iso[0].subjectId).isEqualTo(enrolmentRecord3P1WithBoth.subjectId)

        assertThat(loadedP1A2M1NecEmpty).isEmpty()

        verify(exactly = 2) { mockCallback() }
    }

    @Test
    fun `load - by attendantId and moduleId - should return matching subjects`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedP1A1M1 = dataSource.load(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                attendantId = ATTENDANT_1_ID,
                moduleId = MODULE_1_ID,
            ),
        )
        val loadedP1A1M2 = dataSource.load(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                attendantId = ATTENDANT_1_ID,
                moduleId = MODULE_2_ID,
            ),
        )
        val loadedP1A2M1 = dataSource.load(
            EnrolmentRecordQuery(
                projectId = PROJECT_1_ID,
                attendantId = ATTENDANT_2_ID,
                moduleId = MODULE_1_ID,
            ),
        )
        val loadedP2A2M2 = dataSource.load(
            EnrolmentRecordQuery(
                projectId = PROJECT_2_ID,
                attendantId = ATTENDANT_2_ID,
                moduleId = MODULE_2_ID,
            ),
        )

        // Then
        assertThat(loadedP1A1M1).hasSize(2)
        assertThat(loadedP1A1M1.map { it.subjectId }).containsExactly(
            enrolmentRecord1P1WithFace.subjectId,
            enrolmentRecord2P1WithFinger.subjectId,
        )

        assertThat(loadedP1A1M2).hasSize(1)
        assertThat(loadedP1A1M2[0].subjectId).isEqualTo(enrolmentRecord3P1WithBoth.subjectId)

        assertThat(loadedP1A2M1).hasSize(0)

        assertThat(loadedP2A2M2).hasSize(1)
        assertThat(loadedP2A2M2[0].subjectId).isEqualTo(enrolmentRecord4P2WithBoth.subjectId)
    }

    @Test
    fun `load - by subjectIds - should return only specified subjects`() = runTest {
        // Given
        setupInitialData()
        val targetIds = listOf(
            enrolmentRecord1P1WithFace.subjectId,
            enrolmentRecord4P2WithBoth.subjectId,
            enrolmentRecord6P2WithFinger.subjectId,
        )

        // When
        val loaded = dataSource.load(
            EnrolmentRecordQuery(
                subjectIds = targetIds,
                sort = true,
            ),
        ) // Sort for predictable order

        // Then
        assertThat(loaded).hasSize(3)
        assertThat(loaded.map { it.subjectId })
            .containsExactlyElementsIn(targetIds.sorted())
            .inOrder() // Compare sorted lists
        // Check details of one subject
        val loadedSubject1 = loaded.find { it.subjectId == enrolmentRecord1P1WithFace.subjectId }
        assertThat(loadedSubject1).isNotNull()
        assertThat(loadedSubject1?.attendantId).isEqualTo(ATTENDANT_1_ID)
        assertThat(loadedSubject1?.moduleId).isEqualTo(MODULE_1_ID)
        assertThat(loadedSubject1?.references).isEqualTo(enrolmentRecord1P1WithFace.references)
    }

    @Test
    fun `load - by afterSubjectId - should return subjects after the specified ID`() = runTest {
        // Given
        setupInitialData()
        val allSubjectIdsSorted = listOf(
            enrolmentRecord1P1WithFace.subjectId, // subj-001
            enrolmentRecord2P1WithFinger.subjectId, // subj-002
            enrolmentRecord3P1WithBoth.subjectId, // subj-003
            enrolmentRecord4P2WithBoth.subjectId, // subj-004
            enrolmentRecord5P2WithFace.subjectId, // subj-005
            enrolmentRecord6P2WithFinger.subjectId, // subj-006
        ).sorted()

        val afterId = allSubjectIdsSorted[2] // Should be subj-003

        // When
        // Query for all subjects after subj-003, sorted by ID
        val loaded = dataSource.load(EnrolmentRecordQuery(afterSubjectId = afterId, sort = true))

        // Then
        assertThat(loaded).hasSize(3) // subj-004, subj-005, subj-006 remain
        assertThat(loaded.map { it.subjectId })
            .containsExactly(
                allSubjectIdsSorted[3], // subj-004
                allSubjectIdsSorted[4], // subj-005
                allSubjectIdsSorted[5], // subj-006
            ).inOrder()
    }

    @Test
    fun `getLocalDBInfo returns formatted db info string`() = runTest {
        // Given
        setupInitialData()
        // When
        val result = dataSource.getLocalDBInfo()
        // Then
        assertThat(result).contains("Database Name: db-subjects")
        assertThat(result).contains("Database Version: $SUBJECT_DB_VERSION")
        assertThat(result).contains("Is Encrypted: false") // db not encrypted in tests
        assertThat(result).contains("Number of Subjects: 6")
    }

    @Test
    fun `load - combined query - attendantId, moduleId,  subjectIds - should respect all filters`() = runTest {
        // Given
        setupInitialData()
        // Targets: subj-001, subj-002 (P1, A1, M1), subj-003 (P1, A1, M2)
        val targetIds = listOf(
            enrolmentRecord2P1WithFinger.subjectId, // subj-002
            enrolmentRecord3P1WithBoth.subjectId, // subj-003
            "subj-nonexistent", // Include a non-existent ID
        )

        // Query: Project 1, Attendant 1,  from the targetIds list, sorted
        val query = EnrolmentRecordQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            subjectIds = targetIds,
            sort = true,
        )

        // When
        val loaded = dataSource.load(query)

        // Then
        // Expected: subj-002, subj-003 meet all criteria (P1, A1, > subj-001, in targetIds)
        assertThat(loaded).hasSize(2)
        assertThat(loaded.map { it.subjectId })
            .containsExactly(
                enrolmentRecord2P1WithFinger.subjectId, // subj-002
                enrolmentRecord3P1WithBoth.subjectId, // subj-003
            ).inOrder()
    }

    // 3. Test delete by module id

    @Test
    fun `delete - by moduleId - should delete only subjects with matching moduleId`() = runTest {
        // Given
        setupInitialData()
        val initialCount = dataSource.count()
        val countModule1Before = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_1_ID))
        val countModule2Before = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_2_ID))
        val countModule3Before = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_3_ID))

        assertThat(countModule1Before).isEqualTo(2)
        assertThat(countModule2Before).isEqualTo(2)
        assertThat(countModule3Before).isEqualTo(2)

        val queryToDeleteModule1 = EnrolmentRecordQuery(moduleId = MODULE_1_ID)

        // When
        dataSource.delete(listOf(queryToDeleteModule1))

        // Then
        val finalCount = dataSource.count()
        val countModule1After = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_1_ID))
        val countModule2After = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_2_ID))
        val countModule3After = dataSource.count(EnrolmentRecordQuery(moduleId = MODULE_3_ID))

        assertThat(countModule1After).isEqualTo(0) // All M1 deleted
        assertThat(countModule2After).isEqualTo(2) // M2 untouched
        assertThat(countModule3After).isEqualTo(2) // M3 untouched
        assertThat(finalCount).isEqualTo(initialCount - countModule1Before) // Total count reduced correctly

        // Verify specific subjects are gone/remain
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord1P1WithFace.subjectId))).isEmpty()
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord2P1WithFinger.subjectId))).isEmpty()
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord5P2WithFace.subjectId))).isNotEmpty()
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord3P1WithBoth.subjectId))).isNotEmpty()
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord4P2WithBoth.subjectId))).isNotEmpty()
        assertThat(dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord6P2WithFinger.subjectId))).isNotEmpty()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `delete - by moduleId with format specified - should throw exception`() = runTest {
        // Given
        setupInitialData()
        val queryToDeleteModule1WithFormat = EnrolmentRecordQuery(
            moduleId = MODULE_1_ID,
            format = ROC_1_FORMAT, // Adding format which is not allowed for delete
        )

        // When
        dataSource.delete(listOf(queryToDeleteModule1WithFormat)) // This should throw

        // Then: Exception expected
    }

    @Test
    fun `delete all - should delete all subjects`() = runTest {
        // Given
        setupInitialData()
        val initialCount = dataSource.count()

        // When
        dataSource.deleteAll()

        // Then
        val finalCount = dataSource.count()
        assertThat(finalCount).isEqualTo(0)
        assertThat(initialCount).isGreaterThan(0)
    }

    @Test
    fun `closeOpenDbConnection should close the database `() = runTest {
        // Given
        val mockedDb = mockk<SubjectsDatabase>(relaxed = true)
        val mockedDbFactory = mockk<SubjectsDatabaseFactory> {
            every { get() } returns mockedDb
        }

        dataSource = RoomEnrolmentRecordLocalDataSource(
            timeHelper,
            mockedDbFactory,
            mockk(),
            queryBuilder = RoomEnrolmentRecordQueryBuilder(),
            dispatcherIO = testCoroutineRule.testCoroutineDispatcher,
        )

        // When
        dataSource.closeOpenDbConnection()

        // Then
        verify { mockedDb.close() }
    }

    @Test
    fun `performActions - Update - should succeed when removing all samples but adding external credentials`() = runTest {
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(enrolmentRecord3P1WithBoth)), project)
        val initial = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord3P1WithBoth.subjectId)).first()
        assertThat(initial.references).hasSize(2)
        assertThat(initial.externalCredentials).hasSize(1)

        val newExternalCredential = ExternalCredential(
            id = "new-credential-id",
            value = "new-value".asTokenizableEncrypted(),
            subjectId = enrolmentRecord3P1WithBoth.subjectId,
            type = ExternalCredentialType.NHISCard,
        )

        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord3P1WithBoth.subjectId,
            samplesToAdd = listOf(),
            referenceIdsToRemove = listOf(faceSample2.referenceId, fingerprintSample2.referenceId), // Remove all samples
            externalCredentialsToAdd = listOf(newExternalCredential),
            externalCredentialIdsToRemove = listOf(),
        )

        dataSource.performActions(listOf(updateAction), project)
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord3P1WithBoth.subjectId)).first()
        assertThat(loaded.references).isEmpty()
        assertThat(loaded.externalCredentials).hasSize(2)
        assertThat(loaded.externalCredentials).contains(newExternalCredential)
    }

    @Test
    fun `performActions - Update - should succeed when removing external credentials`() = runTest {
        val subject = enrolmentRecord3P1WithBoth.copy(
            externalCredentials = listOf(
                ExternalCredential(
                    id = "credential-id-1",
                    value = "value-1".asTokenizableEncrypted(),
                    subjectId = enrolmentRecord3P1WithBoth.subjectId,
                    type = ExternalCredentialType.NHISCard,
                ),
                ExternalCredential(
                    id = "credential-id-2",
                    value = "value-2".asTokenizableEncrypted(),
                    subjectId = enrolmentRecord3P1WithBoth.subjectId,
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        )
        dataSource.performActions(listOf(EnrolmentRecordAction.Creation(subject)), project)

        val updateAction = EnrolmentRecordAction.Update(
            subjectId = enrolmentRecord3P1WithBoth.subjectId,
            samplesToAdd = listOf(),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
            externalCredentialIdsToRemove = listOf("credential-id-1"),
        )

        dataSource.performActions(listOf(updateAction), project)
        val loaded = dataSource.load(EnrolmentRecordQuery(subjectId = enrolmentRecord3P1WithBoth.subjectId)).first()
        assertThat(loaded.externalCredentials).hasSize(1)
        assertThat(loaded.externalCredentials.map { it.id }).containsExactly("credential-id-2")
    }

    private fun BiometricReference.copyWithTemplateId(newId: String) = copy(
        templates = templates.map { it.copy(id = newId) },
    )
}
