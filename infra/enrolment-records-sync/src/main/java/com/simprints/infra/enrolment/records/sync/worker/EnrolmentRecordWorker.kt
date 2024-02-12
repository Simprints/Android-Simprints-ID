package com.simprints.infra.enrolment.records.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherIO
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class EnrolmentRecordWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configRepository: ConfigRepository,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
            try {
                val instructionId =
                    inputData.getString(EnrolmentRecordSchedulerImpl.INPUT_ID_NAME)
                        ?: throw IllegalArgumentException("input required")
                val subjectIds =
                    inputData.getStringArray(EnrolmentRecordSchedulerImpl.INPUT_SUBJECT_IDS_NAME)
                        ?: throw IllegalArgumentException("input required")

                enrolmentRecordRepository.uploadRecords(subjectIds.toList())

                configRepository.updateDeviceConfiguration {
                    it.apply { it.lastInstructionId = instructionId }
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
}
