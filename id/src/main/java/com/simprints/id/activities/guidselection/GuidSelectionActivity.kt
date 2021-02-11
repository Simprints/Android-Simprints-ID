package com.simprints.id.activities.guidselection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.GuidSelectionResponse
import com.simprints.id.services.guidselection.GuidSelectionManager
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class GuidSelectionActivity : BaseSplitActivity() {

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var timeHelper: TimeHelper

    @Inject
    lateinit var guidSelectionManager: GuidSelectionManager

    @Inject
    lateinit var crashReportManager: CrashReportManager

    private lateinit var guidSelectionRequest: GuidSelectionRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        injectDependencies()
        Timber.d("GuidSelectionActivity started")

        guidSelectionRequest = intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()

        CoroutineScope(Dispatchers.Main).launch {
            handleGuidSelectionRequest()
        }.invokeOnCompletion {
            // It doesn't matter if it was an error, we always return a good result as before
            sendOkResult()
        }
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    private suspend fun handleGuidSelectionRequest() {
        try {
            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)
            Timber.d("Added Guid Selection Event")
            crashReportManager.logMessageForCrashReport(
                CrashReportTag.SESSION,
                CrashReportTrigger.UI, message = "Added Guid Selection Event"
            )
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logException(t)
        }
    }


    private fun sendOkResult() {
        val response = GuidSelectionResponse(identificationOutcome = true)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, response)
        })

        Timber.d("GuidSelectionActivity done")
        finish()
    }

}
