package com.simprints.id.activities.guidselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentityConfirmationResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.guidselection.GuidSelectionWorker
import com.simprints.id.tools.extensions.parseAppConfirmation
import com.simprints.moduleapi.app.responses.IAppResponse

class GuidSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleGuidSelection()
        sendOkResult()
    }

    private fun scheduleGuidSelection() {
        val guidSelectionWork = buildGuidSelectionWork()
        WorkManager.getInstance().enqueue(guidSelectionWork)
    }

    private fun sendOkResult() {
        val response = AppIdentityConfirmationResponse(identificationOutcome = true)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IAppResponse.BUNDLE_KEY, fromDomainToAppResponse(response))
        })

        finish()
    }

    private fun fromDomainToAppResponse(response: AppResponse): IAppResponse =
        DomainToAppResponse.fromDomainToAppResponse(response)

    private fun buildGuidSelectionWork() = OneTimeWorkRequestBuilder<GuidSelectionWorker>()
        .setInputData(prepareInputData())
        .build()

    private fun prepareInputData() = intent.parseAppConfirmation().toMap().let {
        Data.Builder().putAll(it).build()
    }

}
