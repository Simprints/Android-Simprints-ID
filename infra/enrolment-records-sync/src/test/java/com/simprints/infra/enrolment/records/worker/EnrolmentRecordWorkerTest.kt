package com.simprints.infra.enrolment.records.worker

import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.sync.worker.EnrolmentRecordSchedulerImpl
import com.simprints.infra.enrolment.records.sync.worker.EnrolmentRecordWorker
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EnrolmentRecordWorkerTest {

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }

    private val repository = mockk<EnrolmentRecordRepository>(relaxed = true)
    private val configManager = mockk<ConfigManager>()
    private val params = mockk<WorkerParameters>(relaxed = true) {
        every { inputData } returns workDataOf(
            EnrolmentRecordSchedulerImpl.INPUT_ID_NAME to INSTRUCTION_ID,
            EnrolmentRecordSchedulerImpl.INPUT_SUBJECT_IDS_NAME to arrayOf(SUBJECT_ID),
        )
    }
    private val worker = EnrolmentRecordWorker(
        mockk(relaxed = true),
        params,
        repository,
        configManager,
        UnconfinedTestDispatcher(),
    )


    @Test
    fun `should do work correctly`() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit

        worker.doWork()

        coVerify(exactly = 1) { repository.uploadRecords(listOf(SUBJECT_ID)) }
        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))
        assertThat(updatedConfig.lastInstructionId).isEqualTo(INSTRUCTION_ID)
    }
}
