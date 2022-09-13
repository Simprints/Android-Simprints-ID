package com.simprints.id.enrolmentrecords.worker

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.enrolmentrecords.EnrolmentRecordRepository
import com.simprints.id.services.config.RemoteConfigWorker
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EnrolmentRecordWorker(context: Context, params: WorkerParameters) :
    SimCoroutineWorker(context, params) {

    override val tag: String = EnrolmentRecordWorker::class.java.simpleName

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var repository: EnrolmentRecordRepository

    @Inject
    lateinit var settingsPreferencesManager: SettingsPreferencesManager

    override suspend fun doWork(): Result {
        getComponent<RemoteConfigWorker> { it.inject(this@EnrolmentRecordWorker) }

        return withContext(dispatcherProvider.io()) {
            try {
                crashlyticsLog("Starting")

                val instructionId =
                    inputData.getString(EnrolmentRecordSchedulerImpl.INPUT_ID_NAME)
                        ?: throw IllegalArgumentException("input required")
                val subjectIds =
                    inputData.getStringArray(EnrolmentRecordSchedulerImpl.INPUT_SUBJECT_IDS_NAME)
                        ?: throw IllegalArgumentException("input required")

                repository.uploadRecords(subjectIds.toList())

                settingsPreferencesManager.lastInstructionId = instructionId

                success(message = "Successfully uploaded enrolment records")
            } catch (e: Exception) {
                retry(message = "Failed to upload enrolment records")
            }
        }
    }
}
