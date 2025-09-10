package com.simprints.infra.enrolment.records.repository.local

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource.Simprints
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase.Companion.SUBJECT_DB_VERSION
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabaseFactory
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
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

    // External credentials
    private val externalCredential = ExternalCredential(
        id = "id",
        value = "value".asTokenizableEncrypted(),
        subjectId = "subjectId",
        type = ExternalCredentialType.NHISCard
    )


    // Samples defined first
    private val faceSample1 = FaceSample(
        template = byteArrayOf(1, 2, 3),
        format = ROC_1_FORMAT,
        referenceId = "ref-face-1",
        id = "face-uuid-1",
    )
    private val faceSample2 = FaceSample(
        template = byteArrayOf(4, 5, 6),
        format = ROC_3_FORMAT,
        referenceId = "ref-face-2",
        id = "face-uuid-2",
    )
    private val faceSample3 = FaceSample(
        template = byteArrayOf(7, 8, 9),
        format = ROC_1_FORMAT,
        referenceId = "ref-face-3-p2",
        id = "face-uuid-3-p2",
    )
    private val fingerprintSample1 = FingerprintSample(
        fingerIdentifier = IFingerIdentifier.LEFT_THUMB,
        template = byteArrayOf(10, 11),
        format = NEC_FORMAT,
        referenceId = "ref-fp-1",
        id = "fp-uuid-1",
    )
    private val fingerprintSample2 = FingerprintSample(
        fingerIdentifier = IFingerIdentifier.RIGHT_THUMB,
        template = byteArrayOf(12, 13),
        format = ISO_FORMAT,
        referenceId = "ref-fp-2",
        id = "fp-uuid-2",
    )
    private val fingerprintSample3 = FingerprintSample(
        fingerIdentifier = IFingerIdentifier.LEFT_INDEX_FINGER,
        template = byteArrayOf(14, 15),
        format = NEC_FORMAT,
        referenceId = "ref-fp-3-p2",
        id = "fp-uuid-3-p2",
    )

    // Subjects defined using the samples
    private val subject1P1WithFace = Subject(
        subjectId = "subj-001",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_1_ID,
        faceSamples = listOf(faceSample1),
        fingerprintSamples = emptyList(),
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-001"))
    )
    private val subject2P1WithFinger = Subject(
        subjectId = "subj-002",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_1_ID,
        faceSamples = emptyList(),
        fingerprintSamples = listOf(fingerprintSample1),
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-002"))
    )
    private val subject3P1WithBoth = Subject(
        subjectId = "subj-003",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_2_ID,
        faceSamples = listOf(faceSample2),
        fingerprintSamples = listOf(fingerprintSample2),
        externalCredentials = listOf(getExternalCredential("subj-003"))
    )
    private val subject4P2WithBoth = Subject(
        subjectId = "subj-004",
        projectId = PROJECT_2_ID,
        attendantId = ATTENDANT_2_ID,
        moduleId = MODULE_2_ID,
        faceSamples = listOf(faceSample3),
        fingerprintSamples = listOf(fingerprintSample3),
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-004"))
    )
    private val subject5P2WithFace = Subject(
        // Added subject
        subjectId = "subj-005",
        projectId = PROJECT_2_ID,
        attendantId = ATTENDANT_2_ID,
        moduleId = MODULE_3_ID,
        faceSamples = listOf(faceSample3.copy(id = UUID.randomUUID().toString())),
        fingerprintSamples = emptyList(),
        createdAt = Date(date.time + 1000), // Slightly different time
        updatedAt = Date(date.time + 1000),
        externalCredentials = listOf(getExternalCredential("subj-005"))
    )
    private val subject6P2WithFinger = Subject(
        // Added subject
        subjectId = "subj-006",
        projectId = PROJECT_2_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_3_ID,
        faceSamples = emptyList(),
        fingerprintSamples = listOf(fingerprintSample3.copy(id = UUID.randomUUID().toString())),
        createdAt = Date(date.time + 2000), // Different time
        updatedAt = Date(date.time + 2000),
        externalCredentials = listOf(getExternalCredential("subj-006"))
    )
    private val subjectInvalidNoSamples = Subject(
        subjectId = "subj-invalid",
        projectId = PROJECT_1_ID,
        attendantId = ATTENDANT_1_ID,
        moduleId = MODULE_1_ID,
        createdAt = date,
        updatedAt = date,
        externalCredentials = listOf(getExternalCredential("subj-invalid"))
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
        SubjectAction.Creation(subject1P1WithFace),
        SubjectAction.Creation(subject2P1WithFinger),
        SubjectAction.Creation(subject3P1WithBoth),
        SubjectAction.Creation(subject4P2WithBoth),
        SubjectAction.Creation(subject5P2WithFace),
        SubjectAction.Creation(subject6P2WithFinger),
    )

    private fun getExternalCredential(subjectId: String) = ExternalCredential(
        id = "id",
        value = "value".asTokenizableEncrypted(),
        subjectId = subjectId,
        type = ExternalCredentialType.NHISCard
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
        val action = SubjectAction.Creation(subject1P1WithFace)

        // When
        dataSource.performActions(listOf(action), project)

        // Then
        val loaded = dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))
        assertThat(loaded).hasSize(1)
        val createdSubject = loaded[0]
        assertThat(createdSubject.subjectId).isEqualTo(subject1P1WithFace.subjectId)
        assertThat(createdSubject.faceSamples).hasSize(1)
        assertThat(createdSubject.faceSamples).containsExactly(faceSample1)
        assertThat(createdSubject.fingerprintSamples).isEmpty()
        assertThat(createdSubject.createdAt).isNotNull()
        assertThat(createdSubject.updatedAt).isNotNull()
    }

    @Test
    fun `performActions - Creation - should succeed with fingerprint sample`() = runTest {
        // Given
        val action = SubjectAction.Creation(subject2P1WithFinger)

        // When
        dataSource.performActions(listOf(action), project)

        // Then
        val loaded = dataSource.load(SubjectQuery(subjectId = subject2P1WithFinger.subjectId))
        assertThat(loaded).hasSize(1)
        val createdSubject = loaded[0]
        assertThat(createdSubject.subjectId).isEqualTo(subject2P1WithFinger.subjectId)
        assertThat(createdSubject.faceSamples).isEmpty()
        assertThat(createdSubject.fingerprintSamples).hasSize(1)
        assertThat(createdSubject.fingerprintSamples).containsExactly(fingerprintSample1)
        assertThat(createdSubject.createdAt).isNotNull()
        assertThat(createdSubject.updatedAt).isNotNull()
    }

    @Test
    fun `performActions - Creation - should succeed with both samples`() = runTest {
        // Given:
        val action = SubjectAction.Creation(subject3P1WithBoth)

        // When
        dataSource.performActions(listOf(action), project)

        // Then
        val loaded = dataSource.load(SubjectQuery(subjectId = subject3P1WithBoth.subjectId))
        assertThat(loaded).hasSize(1)
        val createdSubject = loaded[0]
        assertThat(createdSubject.subjectId).isEqualTo(subject3P1WithBoth.subjectId)
        assertThat(createdSubject.faceSamples).hasSize(1)
        assertThat(createdSubject.faceSamples).containsExactly(faceSample2)
        assertThat(createdSubject.fingerprintSamples).hasSize(1)
        assertThat(createdSubject.fingerprintSamples).containsExactly(fingerprintSample2)
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `performActions - Creation - should fail without any samples`() = runTest {
        // Given
        val action = SubjectAction.Creation(subjectInvalidNoSamples) // Subject without samples

        // When
        dataSource.performActions(listOf(action), project) // This line will throw

        // Then: Handled by expected exception.
    }

    @Test
    fun `performActions - Creation - should create multiple valid subjects`() = runTest {
        // Given
        val actions = listOf(
            SubjectAction.Creation(subject1P1WithFace),
            SubjectAction.Creation(subject2P1WithFinger),
            SubjectAction.Creation(subject4P2WithBoth), // Belongs to Project 2
        )

        // When
        dataSource.performActions(actions, project)

        // Then
        val loadedAllP1 = dataSource.load(SubjectQuery(projectId = PROJECT_1_ID, sort = true))
        val loadedAllP2 = dataSource.load(SubjectQuery(projectId = PROJECT_2_ID, sort = true))

        assertThat(loadedAllP1).hasSize(2)
        assertThat(loadedAllP1.map { it.subjectId })
            .containsExactly(
                subject1P1WithFace.subjectId,
                subject2P1WithFinger.subjectId,
            ).inOrder()

        assertThat(loadedAllP2).hasSize(1)
        assertThat(loadedAllP2[0].subjectId).isEqualTo(subject4P2WithBoth.subjectId)
    }

    @Test
    fun `performActions - Update - should add face and fingerprint samples`() = runTest {
        // Given: Create initial subject
        dataSource.performActions(listOf(SubjectAction.Creation(subject1P1WithFace)), project)
        val initialSubject =
            dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId)).first()
        assertThat(initialSubject.faceSamples).hasSize(1)
        assertThat(initialSubject.fingerprintSamples).isEmpty()

        // Original Update action instantiation style maintained
        val updateAction = SubjectAction.Update(
            subjectId = subject1P1WithFace.subjectId,
            faceSamplesToAdd = listOf(faceSample2),
            fingerprintSamplesToAdd = listOf(fingerprintSample1),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded = dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))
        assertThat(loaded).hasSize(1)
        val updatedSubject = loaded[0]
        assertThat(updatedSubject.faceSamples).hasSize(2)
        assertThat(updatedSubject.faceSamples).containsExactly(faceSample1, faceSample2)
        assertThat(updatedSubject.fingerprintSamples).hasSize(1)
        assertThat(updatedSubject.fingerprintSamples).containsExactly(fingerprintSample1)
    }

    @Test
    fun `performActions - Update - should remove face sample when fingerprint sample exists`() = runTest {
        // Given
        val subjectToUpdate = subject3P1WithBoth
        dataSource.performActions(listOf(SubjectAction.Creation(subjectToUpdate)), project)
        val initial =
            dataSource.load(SubjectQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(initial.faceSamples).hasSize(1)
        assertThat(initial.fingerprintSamples).hasSize(1)

        // Original Update action instantiation style maintained
        val updateAction = SubjectAction.Update(
            subjectId = subjectToUpdate.subjectId,
            faceSamplesToAdd = listOf(), // Explicitly empty as in original
            fingerprintSamplesToAdd = listOf(), // Explicitly empty as in original
            referenceIdsToRemove = listOf(faceSample2.referenceId),
            externalCredentialsToAdd = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded =
            dataSource.load(SubjectQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(loaded.faceSamples).isEmpty()
        assertThat(loaded.fingerprintSamples).hasSize(1)
        assertThat(loaded.fingerprintSamples).containsExactly(fingerprintSample2)
    }

    @Test
    fun `performActions - Update - should remove fingerprint sample when face sample exists`() = runTest {
        // Given
        val subjectToUpdate = subject3P1WithBoth
        dataSource.performActions(listOf(SubjectAction.Creation(subjectToUpdate)), project)
        val initial =
            dataSource.load(SubjectQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(initial.faceSamples).hasSize(1)
        assertThat(initial.fingerprintSamples).hasSize(1)

        // Original Update action instantiation style maintained
        val updateAction = SubjectAction.Update(
            subjectId = subjectToUpdate.subjectId,
            faceSamplesToAdd = listOf(),
            fingerprintSamplesToAdd = listOf(),
            referenceIdsToRemove = listOf(fingerprintSample2.referenceId),
            externalCredentialsToAdd = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded =
            dataSource.load(SubjectQuery(subjectId = subjectToUpdate.subjectId)).first()
        assertThat(loaded.faceSamples).hasSize(1)
        assertThat(loaded.faceSamples).containsExactly(faceSample2)
        assertThat(loaded.fingerprintSamples).isEmpty()
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `performActions - Update - should fail when removing last face sample`() = runTest {
        // Given: Subject with only a face sample
        dataSource.performActions(listOf(SubjectAction.Creation(subject1P1WithFace)), project)
        val initial =
            dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId)).first()
        assertThat(initial.faceSamples).hasSize(1)
        assertThat(initial.fingerprintSamples).isEmpty()

        // Original Update action instantiation style maintained
        val updateAction = SubjectAction.Update(
            subjectId = subject1P1WithFace.subjectId,
            faceSamplesToAdd = listOf(),
            fingerprintSamplesToAdd = listOf(),
            referenceIdsToRemove = listOf(faceSample1.referenceId),
            externalCredentialsToAdd = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project) // This line will throw

        // Then: Handled by expected exception
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `performActions - Update - should fail when removing last fingerprint sample`() = runTest {
        // Given: Subject with only a fingerprint sample
        dataSource.performActions(listOf(SubjectAction.Creation(subject2P1WithFinger)), project)
        val initial =
            dataSource.load(SubjectQuery(subjectId = subject2P1WithFinger.subjectId)).first()
        assertThat(initial.faceSamples).isEmpty()
        assertThat(initial.fingerprintSamples).hasSize(1)

        // Original Update action instantiation style maintained
        val updateAction = SubjectAction.Update(
            subjectId = subject2P1WithFinger.subjectId,
            faceSamplesToAdd = listOf(),
            fingerprintSamplesToAdd = listOf(),
            referenceIdsToRemove = listOf(fingerprintSample1.referenceId),
            externalCredentialsToAdd = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project) // This line will throw

        // Then: Handled by expected exception
    }

    @Test
    fun `performActions - Update - should not add duplicate samples based on ID`() = runTest {
        // Given: Subject already has faceSample1
        dataSource.performActions(listOf(SubjectAction.Creation(subject1P1WithFace)), project)

        // Original Update action instantiation style maintained
        val updateAction = SubjectAction.Update(
            subjectId = subject1P1WithFace.subjectId,
            faceSamplesToAdd = listOf(faceSample1, faceSample2),
            fingerprintSamplesToAdd = listOf(fingerprintSample1),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
        )

        // When
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val loaded = dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))
        assertThat(loaded).hasSize(1)
        val finalSubject = loaded[0]

        assertThat(finalSubject.faceSamples).hasSize(2)
        assertThat(finalSubject.faceSamples).containsExactly(faceSample1, faceSample2)

        assertThat(finalSubject.fingerprintSamples).hasSize(1)
        assertThat(finalSubject.fingerprintSamples).containsExactly(fingerprintSample1)
    }

    @Test
    fun `performActions - Update - non-existent subjectId - should do nothing`() = runTest {
        // Given
        setupInitialData()
        val initialCount = dataSource.count()
        val nonExistentSubjectId = "subj-does-not-exist"

        val updateAction = SubjectAction.Update(
            subjectId = nonExistentSubjectId,
            faceSamplesToAdd = listOf(faceSample1), // Try to add samples
            fingerprintSamplesToAdd = listOf(),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
        )

        // When
        // No exception should be thrown here
        dataSource.performActions(listOf(updateAction), project)

        // Then
        val finalCount = dataSource.count()
        assertThat(finalCount).isEqualTo(initialCount) // Count should not change

        // Verify the subject was not created
        val loaded = dataSource.load(SubjectQuery(subjectId = nonExistentSubjectId))
        assertThat(loaded).isEmpty()
    }

    @Test
    fun `load - by no query - should return all subjects`() = runTest {
        // Given
        setupInitialData()

        // When
        val loaded = dataSource.load(SubjectQuery())

        // Then
        assertThat(loaded).hasSize(initialData.size)
    }

    @Test
    fun `load - by subjectId - should return correct subject`() = runTest {
        // Given
        setupInitialData()

        // When
        val loaded = dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))

        // Then
        assertThat(loaded).hasSize(1)
        assertThat(loaded[0].subjectId).isEqualTo(subject1P1WithFace.subjectId)
        assertThat(loaded[0].faceSamples).containsExactly(faceSample1)
        assertThat(loaded[0].fingerprintSamples).isEmpty()
    }

    @Test
    fun `load - by hasUntokenizedFields -should return empty list`() = runTest {
        // Given
        setupInitialData()

        // When
        val loaded = dataSource.load(SubjectQuery(hasUntokenizedFields = true))

        // Then
        assertThat(loaded).isEmpty()
    }

    @Test
    fun `load - by projectId - should return subjects for that project only`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedP1 = dataSource.load(SubjectQuery(projectId = PROJECT_1_ID))
        val loadedP2 = dataSource.load(SubjectQuery(projectId = PROJECT_2_ID))

        // Then
        assertThat(loadedP1).hasSize(3)
        val loadedP1Ids = loadedP1.map { it.subjectId }
        assertThat(loadedP1Ids).containsExactly(
            subject1P1WithFace.subjectId,
            subject2P1WithFinger.subjectId,
            subject3P1WithBoth.subjectId,
        )
        assertThat(loadedP2).hasSize(3)
    }

    @Test
    fun `performActions - Deletion - should delete existing subject`() = runTest {
        // Given
        setupInitialData()
        assertThat(dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))).isNotEmpty()

        val deleteAction = SubjectAction.Deletion(subjectId = subject1P1WithFace.subjectId)

        // When
        dataSource.performActions(listOf(deleteAction), project)

        // Then
        val loaded = dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))
        assertThat(loaded).isEmpty()
        assertThat(dataSource.count()).isEqualTo(initialData.size - 1)
    }

    @Test
    fun `combined actions - create, update, delete`() = runTest {
        // --- Create ---
        val createAction = SubjectAction.Creation(subject1P1WithFace)
        dataSource.performActions(listOf(createAction), project)
        var loadedSubject =
            dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId)).firstOrNull()
        assertThat(loadedSubject).isNotNull()
        assertThat(loadedSubject!!.faceSamples).hasSize(1)
        assertThat(loadedSubject.fingerprintSamples).isEmpty()

        // --- Update (Original style maintained) ---
        val updateAction = SubjectAction.Update(
            subjectId = subject1P1WithFace.subjectId,
            faceSamplesToAdd = listOf(),
            fingerprintSamplesToAdd = listOf(fingerprintSample1),
            referenceIdsToRemove = listOf(),
            externalCredentialsToAdd = listOf(),
        )
        dataSource.performActions(listOf(updateAction), project)
        loadedSubject =
            dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId)).firstOrNull()
        assertThat(loadedSubject).isNotNull()
        assertThat(loadedSubject!!.faceSamples).hasSize(1)
        assertThat(loadedSubject.fingerprintSamples).hasSize(1)
        assertThat(loadedSubject.fingerprintSamples).containsExactly(fingerprintSample1)

        // --- Delete ---
        val deleteAction = SubjectAction.Deletion(subjectId = subject1P1WithFace.subjectId)
        dataSource.performActions(listOf(deleteAction), project)
        val finalLoad = dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))
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
        val countP1 = dataSource.count(SubjectQuery(projectId = PROJECT_1_ID))
        val countP2 = dataSource.count(SubjectQuery(projectId = PROJECT_2_ID))

        // Then
        assertThat(countP1).isEqualTo(3)
        assertThat(countP2).isEqualTo(3)
    }

    @Test
    fun `count - by attendantId - should return count for that attendant`() = runTest {
        // Given
        setupInitialData()

        // When
        val countAtt1 = dataSource.count(SubjectQuery(attendantId = ATTENDANT_1_ID))
        val countAtt2 = dataSource.count(SubjectQuery(attendantId = ATTENDANT_2_ID))

        // Then
        assertThat(countAtt1).isEqualTo(4)
        assertThat(countAtt2).isEqualTo(2)
    }

    @Test
    fun `count - by moduleId - should return count for that module`() = runTest {
        // Given
        setupInitialData()

        // When
        val countModule1 = dataSource.count(SubjectQuery(moduleId = MODULE_1_ID))
        val countModule2 = dataSource.count(SubjectQuery(moduleId = MODULE_2_ID))

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
            SubjectQuery(
                projectId = PROJECT_1_ID,
                faceSampleFormat = ROC_1_FORMAT,
            ),
        )
        val countRoc3P1 = dataSource.count(
            SubjectQuery(
                projectId = PROJECT_1_ID,
                faceSampleFormat = ROC_3_FORMAT,
            ),
        )
        val countRoc1P2 = dataSource.count(
            SubjectQuery(
                projectId = PROJECT_2_ID,
                faceSampleFormat = ROC_1_FORMAT,
            ),
        )
        val countAllRoc1 = dataSource.count(SubjectQuery(faceSampleFormat = ROC_1_FORMAT))

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
            SubjectQuery(
                projectId = PROJECT_1_ID,
                fingerprintSampleFormat = NEC_FORMAT,
            ),
        )
        val countIsoP1 = dataSource.count(
            SubjectQuery(
                projectId = PROJECT_1_ID,
                fingerprintSampleFormat = ISO_FORMAT,
            ),
        )
        val countNecP2 = dataSource.count(
            SubjectQuery(
                projectId = PROJECT_2_ID,
                fingerprintSampleFormat = NEC_FORMAT,
            ),
        )
        val countAllNec = dataSource.count(SubjectQuery(fingerprintSampleFormat = NEC_FORMAT))

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
            SubjectQuery(
                projectId = "non-existent-project",
                moduleId = "non-existent-module".asTokenizableEncrypted(),
                attendantId = "non-existent-attendant".asTokenizableEncrypted(),
            ),
        )

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `loadFingerprintIdentities - should throw exception if format is missing in query`() = runTest {
        // Given
        setupInitialData()
        val queryWithoutFormat = SubjectQuery(projectId = PROJECT_1_ID)

        // When
        dataSource.loadFingerprintIdentities(
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
    fun `loadFingerprintIdentities - should load identities matching format for the specified project`() = runTest {
        // Given
        setupInitialData()
        val project2Mock: Project = mockk { every { id } returns PROJECT_2_ID }

        // When - Query P1 for NEC
        val loadedP1Nec = dataSource
            .loadFingerprintIdentities(
                query = SubjectQuery(
                    projectId = PROJECT_1_ID,
                    fingerprintSampleFormat = NEC_FORMAT,
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
            .loadFingerprintIdentities(
                query = SubjectQuery(
                    projectId = PROJECT_1_ID,
                    fingerprintSampleFormat = ISO_FORMAT,
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
            .loadFingerprintIdentities(
                query = SubjectQuery(
                    projectId = PROJECT_2_ID,
                    fingerprintSampleFormat = NEC_FORMAT,
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
        assertThat(loadedP1Nec[0].subjectId).isEqualTo(subject2P1WithFinger.subjectId)
        assertThat(loadedP1Nec[0].fingerprints).hasSize(1)
        assertThat(loadedP1Nec[0].fingerprints[0].format).isEqualTo(NEC_FORMAT)
        assertThat(loadedP1Nec[0].fingerprints).isEqualTo(subject2P1WithFinger.fingerprintSamples)

        // Then - P1 ISO
        assertThat(loadedP1Iso).hasSize(1)
        assertThat(loadedP1Iso[0].subjectId).isEqualTo(subject3P1WithBoth.subjectId)
        assertThat(loadedP1Iso[0].fingerprints).hasSize(1)
        assertThat(loadedP1Iso[0].fingerprints[0].format).isEqualTo(ISO_FORMAT)
        assertThat(loadedP1Iso[0].fingerprints).isEqualTo(subject3P1WithBoth.fingerprintSamples)

        // Then - P2 NEC
        assertThat(loadedP2Nec).hasSize(2)
        assertThat(loadedP2Nec[0].subjectId).isEqualTo(subject4P2WithBoth.subjectId)
        assertThat(loadedP2Nec[0].fingerprints).hasSize(1)
        assertThat(loadedP2Nec[0].fingerprints[0].format).isEqualTo(NEC_FORMAT)

        verify(exactly = 4) { mockCallback() }
    }

    @Test
    fun `loadFingerprintIdentities - should respect the range parameter for specific format`() = runTest {
        // Given: More data
        val subject5P1WithNec = subject2P1WithFinger.copy(
            subjectId = "subj-005",
            fingerprintSamples = listOf(
                fingerprintSample1.copy(
                    id = "fp-uuid-5",
                    referenceId = "ref-fp-5",
                ),
            ),
            createdAt = Date(date.time + 1000),
        )
        val subject6P1WithNec = subject2P1WithFinger.copy(
            subjectId = "subj-006",
            fingerprintSamples = listOf(
                fingerprintSample1.copy(
                    id = "fp-uuid-6",
                    referenceId = "ref-fp-6",
                ),
            ),
            createdAt = Date(date.time + 2000),
        )
        setupInitialData()
        dataSource.performActions(
            listOf(
                SubjectAction.Creation(subject5P1WithNec),
                SubjectAction.Creation(subject6P1WithNec),
            ),
            project,
        )
        val baseQuery = SubjectQuery(
            projectId = PROJECT_1_ID,
            fingerprintSampleFormat = NEC_FORMAT,
        )
        // When
        val loadedRanges =
            dataSource
                .loadFingerprintIdentities(
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
                .loadFingerprintIdentities(
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
                .loadFingerprintIdentities(
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
        assertThat(loadedRanges[0].identities[0].subjectId).isEqualTo(subject2P1WithFinger.subjectId)
        assertThat(loadedRanges[1].identities).hasSize(1)
        assertThat(loadedRanges[1].identities[0].subjectId).isEqualTo(subject5P1WithNec.subjectId)
        assertThat(loadedFirstTwo).hasSize(2)
        assertThat(loadedFirstTwo.map { it.subjectId })
            .containsExactly(subject2P1WithFinger.subjectId, subject5P1WithNec.subjectId)
            .inOrder()
        assertThat(loadedAll).hasSize(3)
        assertThat(loadedAll.map { it.subjectId })
            .containsExactly(
                subject2P1WithFinger.subjectId,
                subject5P1WithNec.subjectId,
                subject6P1WithNec.subjectId,
            ).inOrder()
        verify(exactly = 7) { mockCallback() }
    }

    @Test
    fun `loadFingerprintIdentities - with query format matching nothing - should return empty list`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedIdentities = dataSource
            .loadFingerprintIdentities(
                query = SubjectQuery(
                    projectId = PROJECT_1_ID,
                    fingerprintSampleFormat = UNUSED_FORMAT,
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

    @Test(expected = IllegalArgumentException::class) // Reverted to JUnit exception check
    fun `loadFaceIdentities - should throw exception if format is missing in query`() = runTest {
        // Given
        setupInitialData()
        val queryWithoutFormat = SubjectQuery(projectId = PROJECT_1_ID)

        // When
        dataSource
            .loadFaceIdentities(
                // This call will throw
                query = queryWithoutFormat,
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()

        // Then: Handled by expected exception
        verify(exactly = 0) { mockCallback() }
    }

    @Test
    fun `loadFaceIdentities - should load identities matching format for the specified project`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedP1Roc1 = dataSource
            .loadFaceIdentities(
                query = SubjectQuery(projectId = PROJECT_1_ID, faceSampleFormat = ROC_1_FORMAT),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP1Roc3 = dataSource
            .loadFaceIdentities(
                query = SubjectQuery(projectId = PROJECT_1_ID, faceSampleFormat = ROC_3_FORMAT),
                ranges = listOf(0..10),
                project = project,
                dataSource = Simprints,
                scope = this,
                onCandidateLoaded = mockCallback,
            ).toList()
            .first()
            .identities
        val loadedP2Roc1 = dataSource
            .loadFaceIdentities(
                query = SubjectQuery(projectId = PROJECT_2_ID, faceSampleFormat = ROC_1_FORMAT),
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
        assertThat(loadedP1Roc1[0].subjectId).isEqualTo(subject1P1WithFace.subjectId)
        assertThat(loadedP1Roc1[0].faces).hasSize(1)
        assertThat(loadedP1Roc1[0].faces[0].format).isEqualTo(ROC_1_FORMAT)
        assertThat(loadedP1Roc1[0].faces).isEqualTo(subject1P1WithFace.faceSamples)

        // Then - P1 ROC_3
        assertThat(loadedP1Roc3).hasSize(1)
        assertThat(loadedP1Roc3[0].subjectId).isEqualTo(subject3P1WithBoth.subjectId)
        assertThat(loadedP1Roc3[0].faces).hasSize(1)
        assertThat(loadedP1Roc3[0].faces[0].format).isEqualTo(ROC_3_FORMAT)
        assertThat(loadedP1Roc3[0].faces).isEqualTo(subject3P1WithBoth.faceSamples)

        // Then - P2 ROC_1
        assertThat(loadedP2Roc1).hasSize(2)
        assertThat(loadedP2Roc1[0].subjectId).isEqualTo(subject4P2WithBoth.subjectId)

        verify(exactly = 4) { mockCallback() }
    }

    @Test
    fun `loadFaceIdentities - should respect the range parameter for specific format`() = runTest {
        // Given: More data
        val subject5P1WithRoc1 = subject1P1WithFace.copy(
            subjectId = "subj-005",
            faceSamples = listOf(faceSample1.copy(id = "face-uuid-5", referenceId = "ref-face-5")),
            createdAt = Date(date.time + 1000),
        )
        val subject6P1WithRoc1 = subject1P1WithFace.copy(
            subjectId = "subj-006",
            faceSamples = listOf(faceSample1.copy(id = "face-uuid-6", referenceId = "ref-face-6")),
            createdAt = Date(date.time + 2000),
        )
        setupInitialData()
        dataSource.performActions(
            listOf(
                SubjectAction.Creation(subject5P1WithRoc1),
                SubjectAction.Creation(subject6P1WithRoc1),
            ),
            project,
        )
        val baseQuery =
            SubjectQuery(projectId = PROJECT_1_ID, faceSampleFormat = ROC_1_FORMAT, sort = true)
        // When
        val loadedRanges =
            dataSource
                .loadFaceIdentities(
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
                .loadFaceIdentities(
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
            .loadFaceIdentities(
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
        assertThat(loadedRanges[0].identities[0].subjectId).isEqualTo(subject1P1WithFace.subjectId)
        assertThat(loadedRanges[1].identities).hasSize(1)
        assertThat(loadedRanges[1].identities[0].subjectId).isEqualTo(subject5P1WithRoc1.subjectId)
        assertThat(loadedFirstTwo).hasSize(2)
        assertThat(loadedFirstTwo.map { it.subjectId })
            .containsExactly(subject1P1WithFace.subjectId, subject5P1WithRoc1.subjectId)
            .inOrder()
        assertThat(loadedAll).hasSize(3)
        assertThat(loadedAll.map { it.subjectId })
            .containsExactly(
                subject1P1WithFace.subjectId,
                subject5P1WithRoc1.subjectId,
                subject6P1WithRoc1.subjectId,
            ).inOrder()
        verify(exactly = 7) { mockCallback() }
    }

    @Test
    fun `loadFaceIdentities - with query format matching nothing - should return empty list`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedIdentities = dataSource
            .loadFaceIdentities(
                query = SubjectQuery(
                    projectId = PROJECT_1_ID,
                    faceSampleFormat = UNUSED_FORMAT,
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
    fun `loadFaceIdentities - by attendantId and moduleId - should return correct identities`() = runTest {
        // Given
        setupInitialData()

        // Query for Project 1, Attendant 1, Module 1, Format ROC_1
        val queryP1A1M1Roc1 = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_1_ID,
            faceSampleFormat = ROC_1_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 2, Format ROC_3
        val queryP1A1M2Roc3 = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_2_ID,
            faceSampleFormat = ROC_3_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 1, Format ROC_3 (should be empty)
        val queryP1A1M1Roc3Empty = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_1_ID,
            faceSampleFormat = ROC_3_FORMAT,
        )

        // When
        val loadedP1A1M1Roc1 = dataSource
            .loadFaceIdentities(
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
            .loadFaceIdentities(
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
                .loadFaceIdentities(
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
        assertThat(loadedP1A1M1Roc1[0].subjectId).isEqualTo(subject1P1WithFace.subjectId)

        assertThat(loadedP1A1M2Roc3).hasSize(1)
        assertThat(loadedP1A1M2Roc3[0].subjectId).isEqualTo(subject3P1WithBoth.subjectId)

        assertThat(loadedP1A1M1Roc3Empty).isEmpty()

        verify(exactly = 2) { mockCallback() } // Called for the 3 successful loads
    }

    @Test
    fun `loadFingerprintIdentities - by attendantId and moduleId - should return correct identities`() = runTest {
        // Given
        setupInitialData()

        // Query for Project 1, Attendant 1, Module 1, Format NEC
        val queryP1A1M1Nec = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_1_ID,
            fingerprintSampleFormat = NEC_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 3, Format NEC
        val queryP1A1M3Nec = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_3_ID,
            fingerprintSampleFormat = NEC_FORMAT,
        )

        // Query for Project 1, Attendant 1, Module 2, Format ISO
        val queryP1A1M2Iso = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_1_ID,
            moduleId = MODULE_2_ID,
            fingerprintSampleFormat = ISO_FORMAT,
        )

        // Query for Project 1, Attendant 2, Module 1, Format NEC (should be empty)
        val queryP1A2M1NecEmpty = SubjectQuery(
            projectId = PROJECT_1_ID,
            attendantId = ATTENDANT_2_ID,
            moduleId = MODULE_1_ID,
            fingerprintSampleFormat = NEC_FORMAT,
        )

        // When
        val loadedP1A1M1Nec = dataSource
            .loadFingerprintIdentities(
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
            .loadFingerprintIdentities(
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
            .loadFingerprintIdentities(
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
            .loadFingerprintIdentities(
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
        assertThat(loadedP1A1M1Nec[0].subjectId).isEqualTo(subject2P1WithFinger.subjectId)

        assertThat(loadedP1A1M3Nec).hasSize(0)

        assertThat(loadedP1A1M2Iso).hasSize(1)
        assertThat(loadedP1A1M2Iso[0].subjectId).isEqualTo(subject3P1WithBoth.subjectId)

        assertThat(loadedP1A2M1NecEmpty).isEmpty()

        verify(exactly = 2) { mockCallback() }
    }

    @Test
    fun `load - by attendantId and moduleId - should return matching subjects`() = runTest {
        // Given
        setupInitialData()

        // When
        val loadedP1A1M1 = dataSource.load(
            SubjectQuery(
                projectId = PROJECT_1_ID,
                attendantId = ATTENDANT_1_ID,
                moduleId = MODULE_1_ID,
            ),
        )
        val loadedP1A1M2 = dataSource.load(
            SubjectQuery(
                projectId = PROJECT_1_ID,
                attendantId = ATTENDANT_1_ID,
                moduleId = MODULE_2_ID,
            ),
        )
        val loadedP1A2M1 = dataSource.load(
            SubjectQuery(
                projectId = PROJECT_1_ID,
                attendantId = ATTENDANT_2_ID,
                moduleId = MODULE_1_ID,
            ),
        )
        val loadedP2A2M2 = dataSource.load(
            SubjectQuery(
                projectId = PROJECT_2_ID,
                attendantId = ATTENDANT_2_ID,
                moduleId = MODULE_2_ID,
            ),
        )

        // Then
        assertThat(loadedP1A1M1).hasSize(2)
        assertThat(loadedP1A1M1.map { it.subjectId }).containsExactly(
            subject1P1WithFace.subjectId,
            subject2P1WithFinger.subjectId,
        )

        assertThat(loadedP1A1M2).hasSize(1)
        assertThat(loadedP1A1M2[0].subjectId).isEqualTo(subject3P1WithBoth.subjectId)

        assertThat(loadedP1A2M1).hasSize(0)

        assertThat(loadedP2A2M2).hasSize(1)
        assertThat(loadedP2A2M2[0].subjectId).isEqualTo(subject4P2WithBoth.subjectId)
    }

    @Test
    fun `load - by subjectIds - should return only specified subjects`() = runTest {
        // Given
        setupInitialData()
        val targetIds = listOf(
            subject1P1WithFace.subjectId,
            subject4P2WithBoth.subjectId,
            subject6P2WithFinger.subjectId,
        )

        // When
        val loaded = dataSource.load(
            SubjectQuery(
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
        val loadedSubject1 = loaded.find { it.subjectId == subject1P1WithFace.subjectId }
        assertThat(loadedSubject1).isNotNull()
        assertThat(loadedSubject1?.attendantId).isEqualTo(ATTENDANT_1_ID)
        assertThat(loadedSubject1?.moduleId).isEqualTo(MODULE_1_ID)
        assertThat(loadedSubject1?.faceSamples).isEqualTo(subject1P1WithFace.faceSamples)
    }

    @Test
    fun `load - by afterSubjectId - should return subjects after the specified ID`() = runTest {
        // Given
        setupInitialData()
        val allSubjectIdsSorted = listOf(
            subject1P1WithFace.subjectId, // subj-001
            subject2P1WithFinger.subjectId, // subj-002
            subject3P1WithBoth.subjectId, // subj-003
            subject4P2WithBoth.subjectId, // subj-004
            subject5P2WithFace.subjectId, // subj-005
            subject6P2WithFinger.subjectId, // subj-006
        ).sorted()

        val afterId = allSubjectIdsSorted[2] // Should be subj-003

        // When
        // Query for all subjects after subj-003, sorted by ID
        val loaded = dataSource.load(SubjectQuery(afterSubjectId = afterId, sort = true))

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
            subject2P1WithFinger.subjectId, // subj-002
            subject3P1WithBoth.subjectId, // subj-003
            "subj-nonexistent", // Include a non-existent ID
        )

        // Query: Project 1, Attendant 1,  from the targetIds list, sorted
        val query = SubjectQuery(
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
                subject2P1WithFinger.subjectId, // subj-002
                subject3P1WithBoth.subjectId, // subj-003
            ).inOrder()
    }

    // 3. Test delete by module id

    @Test
    fun `delete - by moduleId - should delete only subjects with matching moduleId`() = runTest {
        // Given
        setupInitialData()
        val initialCount = dataSource.count()
        val countModule1Before = dataSource.count(SubjectQuery(moduleId = MODULE_1_ID))
        val countModule2Before = dataSource.count(SubjectQuery(moduleId = MODULE_2_ID))
        val countModule3Before = dataSource.count(SubjectQuery(moduleId = MODULE_3_ID))

        assertThat(countModule1Before).isEqualTo(2)
        assertThat(countModule2Before).isEqualTo(2)
        assertThat(countModule3Before).isEqualTo(2)

        val queryToDeleteModule1 = SubjectQuery(moduleId = MODULE_1_ID)

        // When
        dataSource.delete(listOf(queryToDeleteModule1))

        // Then
        val finalCount = dataSource.count()
        val countModule1After = dataSource.count(SubjectQuery(moduleId = MODULE_1_ID))
        val countModule2After = dataSource.count(SubjectQuery(moduleId = MODULE_2_ID))
        val countModule3After = dataSource.count(SubjectQuery(moduleId = MODULE_3_ID))

        assertThat(countModule1After).isEqualTo(0) // All M1 deleted
        assertThat(countModule2After).isEqualTo(2) // M2 untouched
        assertThat(countModule3After).isEqualTo(2) // M3 untouched
        assertThat(finalCount).isEqualTo(initialCount - countModule1Before) // Total count reduced correctly

        // Verify specific subjects are gone/remain
        assertThat(dataSource.load(SubjectQuery(subjectId = subject1P1WithFace.subjectId))).isEmpty()
        assertThat(dataSource.load(SubjectQuery(subjectId = subject2P1WithFinger.subjectId))).isEmpty()
        assertThat(dataSource.load(SubjectQuery(subjectId = subject5P2WithFace.subjectId))).isNotEmpty()
        assertThat(dataSource.load(SubjectQuery(subjectId = subject3P1WithBoth.subjectId))).isNotEmpty()
        assertThat(dataSource.load(SubjectQuery(subjectId = subject4P2WithBoth.subjectId))).isNotEmpty()
        assertThat(dataSource.load(SubjectQuery(subjectId = subject6P2WithFinger.subjectId))).isNotEmpty()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `delete - by moduleId with format specified - should throw exception`() = runTest {
        // Given
        setupInitialData()
        val queryToDeleteModule1WithFormat = SubjectQuery(
            moduleId = MODULE_1_ID,
            faceSampleFormat = ROC_1_FORMAT, // Adding format which is not allowed for delete
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
}
