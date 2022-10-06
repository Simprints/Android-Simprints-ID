package com.simprints.infra.enrolment.records.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// @HiltWorker
class EnrolmentRecordWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val manager: EnrolmentRecordManager,
    private val configManager: ConfigManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
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

                manager.uploadRecords(subjectIds.toList())

                configManager.updateDeviceConfiguration {
                    it.apply { it.lastInstructionId = instructionId }
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
}
