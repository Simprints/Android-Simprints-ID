package com.simprints.id.activities.guidselection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.R
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.GuidSelectionResponse
import com.simprints.id.services.guidselection.GuidSelectionManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GuidSelectionActivity : BaseSplitActivity() {

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var guidSelectionManager: GuidSelectionManager

    private lateinit var guidSelectionRequest: GuidSelectionRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        Simber.d("GuidSelectionActivity started")

        guidSelectionRequest =
            intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()

        CoroutineScope(Dispatchers.Main).launch {
            handleGuidSelectionRequest()
        }.invokeOnCompletion {
            // It doesn't matter if it was an error, we always return a good result as before
            sendOkResult()
        }
    }

    @SuppressLint("CheckResult")
    private suspend fun handleGuidSelectionRequest() {
        try {
            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)
            Simber.tag(CrashReportTag.SESSION.name).i("Added Guid Selection Event")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }


    private fun sendOkResult() {
        val response = GuidSelectionResponse(identificationOutcome = true)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, response)
        })

        Simber.d("GuidSelectionActivity done")
        finish()
    }

}
