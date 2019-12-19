package com.simprints.id.activities.guidselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.ConfirmationCallbackEvent
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppConfirmationResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.guidselection.GuidSelectionWorker
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.parseAppConfirmation
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

class GuidSelectionActivity : AppCompatActivity() {

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var timeHelper: TimeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectDependencies()

        scheduleGuidSelection()
        sendOkResult()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun scheduleGuidSelection() {
        val guidSelectionWork = buildGuidSelectionWork()
        WorkManager.getInstance().enqueue(guidSelectionWork)
    }

    private fun sendOkResult() {
        val response = AppConfirmationResponse(identificationOutcome = true)
        addConfirmationCallbackEvent(response)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IAppResponse.BUNDLE_KEY, fromDomainToAppResponse(response))
        })

        finish()
    }

    private fun fromDomainToAppResponse(response: AppResponse): IAppResponse =
        DomainToModuleApiAppResponse.fromDomainModuleApiAppResponse(response)

    private fun buildGuidSelectionWork() = OneTimeWorkRequestBuilder<GuidSelectionWorker>()
        .setInputData(prepareInputData())
        .build()

    private fun prepareInputData() = intent.parseAppConfirmation().toMap().let {
        Data.Builder().putAll(it).build()
    }

    private fun addConfirmationCallbackEvent(response: AppConfirmationResponse) {
        sessionEventsManager.addEventInBackground(
            ConfirmationCallbackEvent(timeHelper.now(), response.identificationOutcome)
        )
    }
}
