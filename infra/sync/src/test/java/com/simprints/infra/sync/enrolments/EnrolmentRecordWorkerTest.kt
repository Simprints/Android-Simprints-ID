package com.simprints.infra.sync.enrolments

import android.os.PowerManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.sync.SyncConstants
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EnrolmentRecordWorkerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var recordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var params: WorkerParameters

    private lateinit var worker: EnrolmentRecordWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { params.inputData } returns workDataOf(
            SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME to INSTRUCTION_ID,
            SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME to arrayOf(SUBJECT_ID),
        )

        worker = EnrolmentRecordWorker(
            mockk(relaxed = true) {
                every { getSystemService<PowerManager>(any()) } returns mockk {
                    every { isIgnoringBatteryOptimizations(any()) } returns true
                }
            },
            params,
            recordRepository,
            configRepository,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `should do work correctly`() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configRepository.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit

        worker.doWork()

        coVerify(exactly = 1) { recordRepository.uploadRecords(listOf(SUBJECT_ID)) }
        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))
        assertThat(updatedConfig.lastInstructionId).isEqualTo(INSTRUCTION_ID)
    }

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }
}
