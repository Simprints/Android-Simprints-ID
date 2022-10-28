package com.simprints.infra.enrolment.records.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EnrolmentRecordSchedulerImplTest {

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }

    private val ctx = mockk<Context>()
    private val workManager = mockk<WorkManager>(relaxed = true)
    private lateinit var enrolmentRecordSchedulerImpl: EnrolmentRecordSchedulerImpl

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        enrolmentRecordSchedulerImpl = EnrolmentRecordSchedulerImpl(ctx)
    }

    @Test
    fun `upload should schedule the worker with the correct data`() = runTest {
        enrolmentRecordSchedulerImpl.upload(INSTRUCTION_ID, listOf(SUBJECT_ID))

        coVerify(exactly = 1) {
            workManager.enqueueUniqueWork(
                any(),
                ExistingWorkPolicy.KEEP,
                match<OneTimeWorkRequest> { oneTimeWorkRequest ->
                    val subjectIdsInput = oneTimeWorkRequest.workSpec.input.getStringArray(
                        EnrolmentRecordSchedulerImpl.INPUT_SUBJECT_IDS_NAME
                    )
                    val instructionIdInput =
                        oneTimeWorkRequest.workSpec.input.getString(EnrolmentRecordSchedulerImpl.INPUT_ID_NAME)
                    instructionIdInput == INSTRUCTION_ID && subjectIdsInput.contentEquals(
                        arrayOf(
                            SUBJECT_ID
                        )
                    )
                }
            )
        }
    }
}
