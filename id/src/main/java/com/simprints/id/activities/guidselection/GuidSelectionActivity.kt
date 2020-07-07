package com.simprints.id.activities.guidselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.event.SessionRepository
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.guidselection.GuidSelectionWorker
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.GuidSelectionResponse
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import javax.inject.Inject

class GuidSelectionActivity : AppCompatActivity() {

    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var timeHelper: TimeHelper
    private lateinit var guildSelectionRequest: GuidSelectionRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        injectDependencies()
        Timber.d("GuidSelectionActivity started")

        guildSelectionRequest = intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()

        scheduleGuidSelection()
        sendOkResult()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun scheduleGuidSelection() {
        val guidSelectionWork = buildGuidSelectionWork()
        WorkManager.getInstance(this).enqueue(guidSelectionWork)
    }

    private fun sendOkResult() {
        val response = GuidSelectionResponse(identificationOutcome = true)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, response)
        })

        Timber.d("GuidSelectionActivity done")
        finish()
    }

    private fun buildGuidSelectionWork() = OneTimeWorkRequestBuilder<GuidSelectionWorker>()
        .setInputData(prepareInputData())
        .build()

    private fun prepareInputData() = Data.Builder().putAll(guildSelectionRequest.toMap()).build()
}
