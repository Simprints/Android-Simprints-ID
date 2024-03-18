package com.simprints.infra.sync.enrolments

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherIO
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.sync.SyncConstants
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
) : SimCoroutineWorker(context, params) {

    override val tag: String = "EnrolmentRecordWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Enrolment record upload start")
        try {
            val instructionId =
                inputData.getString(SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME)
                    ?: throw IllegalArgumentException("input required")
            val subjectIds =
                inputData.getStringArray(SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME)
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
