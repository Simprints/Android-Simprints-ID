package com.simprints.id.activities.identity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.id.identity.GuidSelectionWorker
import com.simprints.id.tools.extensions.parseAppConfirmation
import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation

class GuidSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleGuidSelection()
        finish()
    }

    private fun scheduleGuidSelection() {
        val guidSelectionWork = buildGuidSelectionWork()
        WorkManager.getInstance().enqueue(guidSelectionWork)
    }

    private fun buildGuidSelectionWork(): OneTimeWorkRequest {
        val inputData = prepareInputData()
        return OneTimeWorkRequestBuilder<GuidSelectionWorker>()
            .setInputData(inputData)
            .build()
    }

    private fun prepareInputData(): Data {
        val requestJson = intent.parseAppConfirmation().toJson()
        return Data.Builder().putString(IAppConfirmation.BUNDLE_KEY, requestJson).build()
    }

}
