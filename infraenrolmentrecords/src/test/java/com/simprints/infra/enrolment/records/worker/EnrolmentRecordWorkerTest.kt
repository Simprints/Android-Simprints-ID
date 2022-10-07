package com.simprints.infra.enrolment.records.worker

import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EnrolmentRecordWorkerTest {

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }

    private val manager = mockk<EnrolmentRecordManager>(relaxed = true)
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
        manager,
        configManager,
    )


    @Test
    fun `should do work correctly`() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit

        worker.doWork()

        coVerify(exactly = 1) { manager.uploadRecords(listOf(SUBJECT_ID)) }
        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), listOf(), ""))
        assertThat(updatedConfig.lastInstructionId).isEqualTo(INSTRUCTION_ID)
    }
}
