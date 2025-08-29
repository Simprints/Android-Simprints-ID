package com.simprints.feature.datagenerator

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.feature.datagenerator.enrollmentrecords.InsertEnrollmentRecordsUseCase
import com.simprints.feature.datagenerator.enrollmentrecords.InsertEnrollmentRecordsUseCase.Companion.BATCH_SIZE
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class InsertEnrollmentRecordsUseCaseTest {
    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var configRepository: ConfigRepository

    private lateinit var useCase: InsertEnrollmentRecordsUseCase
    private var templates: Bundle = bundleOf(
        Pair("ISO_19794_2", 1),
        Pair("RANK_ONE_3_1", 1),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { configRepository.getProject() } returns mockk<Project>()
        useCase = InsertEnrollmentRecordsUseCase(
            enrolmentRecordRepository,
            configRepository,
            mockk(relaxed = true),
            UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun `invoke with zero records should emit final message and not call repository`() = runTest {
        // When
        val result = useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = 0,
            templatesPerFormat = templates,
            firstSubjectId = "",
            fingerOrder = null,
        ).last()

        // Then
        assertThat(result).isEqualTo("Inserted 0 biometric records")
        coVerify(exactly = 0) { enrolmentRecordRepository.performActions(any(), any()) }
    }

    @Test
    fun `invoke with less than batch size records should insert one batch`() = runTest {
        // Given
        val numRecords = 10
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        val result = useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = numRecords,
            templatesPerFormat = templates,
            firstSubjectId = "",
            fingerOrder = null,
        ).last()

        // Then
        assertThat(result).isEqualTo("Inserted $numRecords biometric records")
        coVerify(exactly = 1) { enrolmentRecordRepository.performActions(any(), any()) }
        assertThat(subjectActionsSlot.captured).hasSize(numRecords)
    }

    @Test
    fun `invoke with exactly batch size records should insert one full batch`() = runTest {
        // Given
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        val result = useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = BATCH_SIZE,
            templatesPerFormat = templates,
            firstSubjectId = "",
            fingerOrder = null,
        ).last()

        // Then
        assertThat(result).isEqualTo("Inserted $BATCH_SIZE biometric records")
        coVerify(exactly = 1) { enrolmentRecordRepository.performActions(any(), any()) }
        assertThat(subjectActionsSlot.captured).hasSize(BATCH_SIZE)
    }

    @Test
    fun `invoke with more than batch size records should insert multiple batches`() = runTest {
        // Given
        val numRecords = BATCH_SIZE + 10
        val capturedActions = mutableListOf<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(capturedActions), any()) } returns mockk()

        // When
        val result = useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = numRecords,
            templatesPerFormat = templates,
            firstSubjectId = "",
            fingerOrder = null,
        ).last()

        // Then
        assertThat(result).isEqualTo("Inserted $numRecords biometric records")
        coVerify(exactly = 2) { enrolmentRecordRepository.performActions(any(), any()) }
        assertThat(capturedActions[0]).hasSize(BATCH_SIZE)
        assertThat(capturedActions[1]).hasSize(10)
    }

    @Test
    fun `invoke with firstSubjectId should use it for the first record only`() = runTest {
        // Given
        val numRecords = 3
        val firstId = "test-subject-id-123"
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = numRecords,
            templatesPerFormat = templates,
            firstSubjectId = firstId,
            fingerOrder = null,
        ).last()

        // Then
        val subjects = subjectActionsSlot.captured.map { it.subject }
        assertThat(subjects).hasSize(numRecords)
        assertThat(subjects[0].subjectId).isEqualTo(firstId)
        assertThat(subjects[1].subjectId).isNotEqualTo(firstId)
        assertThat(subjects[2].subjectId).isNotEqualTo(firstId)
        assertThat(subjects[1].subjectId).isNotEqualTo(subjects[2].subjectId)
    }

    @Test
    fun `invoke with blank firstSubjectId should generate random IDs for all records`() = runTest {
        // Given
        val numRecords = 3
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = numRecords,
            templatesPerFormat = templates,
            firstSubjectId = " ",
            fingerOrder = null,
        ).last()

        // Then
        val subjects = subjectActionsSlot.captured.map { it.subject }
        assertThat(subjects).hasSize(numRecords)
        assertThat(subjects[0].subjectId).isNotEmpty()
        assertThat(subjects[0].subjectId).isNotEqualTo(" ")
        assertThat(subjects[1].subjectId).isNotEmpty()
        assertThat(subjects[0].subjectId).isNotEqualTo(subjects[1].subjectId)
    }

    @Test
    fun `invoke should generate correct number of face and fingerprint samples`() = runTest {
        // Given
        val templatesPerFormat = Bundle().apply {
            putInt("SIM_FACE_BASE_1", 2) // 2 face samples
            putInt("NEC_1_5", 6) // 6 fingerprint samples
        }
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = 1,
            templatesPerFormat = templatesPerFormat,
            firstSubjectId = "",
            fingerOrder = bundleOf(
                "NEC_1_5" to
                    "LEFT_3RD_FINGER,LEFT_4TH_FINGER,LEFT_5TH_FINGER,RIGHT_3RD_FINGER,RIGHT_4TH_FINGER,RIGHT_5TH_FINGER",
            ),
        ).last()

        // Then
        val subject = subjectActionsSlot.captured.first().subject
        assertThat(subject.faceSamples).hasSize(2)
        assertThat(subject.faceSamples.all { it.format == "SIM_FACE_BASE_1" }).isTrue()
        assertThat(subject.fingerprintSamples).hasSize(6)
        assertThat(subject.fingerprintSamples.all { it.format == "NEC_1_5" }).isTrue()
    }

    @Test
    fun `invoke should use default finger identifier when fingerOrder is null`() = runTest {
        // Given
        val templatesPerFormat = Bundle().apply {
            putInt("ISO_19794_2", 2)
        }
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = 1,
            templatesPerFormat = templatesPerFormat,
            firstSubjectId = "",
            fingerOrder = null, // No finger order provided
        ).last()

        // Then
        val subject = subjectActionsSlot.captured.first().subject
        assertThat(subject.fingerprintSamples).hasSize(2)
        assertThat(subject.fingerprintSamples[0].fingerIdentifier).isEqualTo(SampleIdentifier.LEFT_THUMB)
        assertThat(subject.fingerprintSamples[1].fingerIdentifier).isEqualTo(SampleIdentifier.LEFT_THUMB)
    }

    @Test
    fun `invoke should use and cycle through fingerOrder`() = runTest {
        // Given
        val format = "ISO_19794_2"
        val templatesPerFormat = Bundle().apply {
            putInt(format, 4) // 4 samples
        }
        val fingerOrder = Bundle().apply {
            // 2 fingers, so it should cycle
            putString(format, "RIGHT_THUMB,RIGHT_INDEX_FINGER")
        }
        val subjectActionsSlot = slot<List<SubjectAction.Creation>>()
        coEvery { enrolmentRecordRepository.performActions(capture(subjectActionsSlot), any()) } returns mockk()

        // When
        useCase(
            projectId = "p1",
            moduleId = "m1",
            attendantId = "a1",
            numRecords = 1,
            templatesPerFormat = templatesPerFormat,
            firstSubjectId = "",
            fingerOrder = fingerOrder,
        ).last()

        // Then
        val subject = subjectActionsSlot.captured.first().subject
        val fingers = subject.fingerprintSamples.map { it.fingerIdentifier }
        assertThat(fingers).hasSize(4)
        assertThat(fingers)
            .containsExactly(
                SampleIdentifier.RIGHT_THUMB,
                SampleIdentifier.RIGHT_INDEX_FINGER,
                SampleIdentifier.RIGHT_THUMB, // cycles back
                SampleIdentifier.RIGHT_INDEX_FINGER,
            ).inOrder()
    }
}
