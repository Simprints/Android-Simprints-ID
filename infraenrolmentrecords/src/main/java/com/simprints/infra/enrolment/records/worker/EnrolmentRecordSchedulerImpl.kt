package com.simprints.infra.enrolment.records.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class EnrolmentRecordSchedulerImpl @Inject constructor(@ApplicationContext context: Context) :
    EnrolmentRecordScheduler {

    companion object {
        private const val WORK_NAME = "upload-enrolment-record-work-one-time"
        const val INPUT_ID_NAME = "INPUT_ID_NAME"
        const val INPUT_SUBJECT_IDS_NAME = "INPUT_SUBJECT_IDS_NAME"
    }

    private val workManager = WorkManager.getInstance(context)

    override fun upload(id: String, subjectIds: List<String>) {
        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest(id, subjectIds)
        )
    }

    private fun buildOneTimeRequest(id: String, subjectIds: List<String>): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<EnrolmentRecordWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    INPUT_SUBJECT_IDS_NAME to subjectIds.toTypedArray(),
                    INPUT_ID_NAME to id
                )
            )
            .build()
}

