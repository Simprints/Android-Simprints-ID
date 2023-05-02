package com.simprints.id.activities.fetchguid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ShowExitFormWrapper
import com.simprints.id.R
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.orchestrator.steps.core.requests.FetchGUIDRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.FetchGUIDResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FetchGuidActivity : BaseSplitActivity() {

    private lateinit var fetchGuidRequest: FetchGUIDRequest
    private val viewModel: FetchGuidViewModel by viewModels()

    @Inject
    lateinit var exitFormHelper: ExitFormHelper

    private val openWifiSettings = registerForActivityResult(
        object : ActivityResultContract<Unit, Unit>() {
            override fun createIntent(context: Context, input: Unit) =
                Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)

            override fun parseResult(resultCode: Int, intent: Intent?) {}
        }
    ) { tryToFetchGuid() }

    private val openExitForm = registerForActivityResult(ShowExitFormWrapper()) { data ->
        val result = exitFormHelper.buildExitFormResponse(data)
        if (result != null) {
            setResultAndFinish(result)
        } else finish()
    }

    private val showAlert = registerForActivityResult(ShowAlertWrapper()) {
        val alertType = AlertType.fromPayload(it)

        when (AlertContract.getResponseKey(it)) {
            AlertType.ACTION_CLOSE -> setResultAndFinish(FetchGUIDResponse(false))
            AlertType.ACTION_WIFI_SETTINGS -> openWifiSettings.launch(Unit)
            AlertType.ACTION_RETRY -> tryToFetchGuid()
            AlertContract.ALERT_BUTTON_PRESSED_BACK -> {
                if (alertType == AlertType.GUID_NOT_FOUND_OFFLINE) {
                    viewModel.startExitForm()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_guid)

        setupObserversForUi()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                fetchGuidRequest =
                    intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()
                tryToFetchGuid()
            }
        }
    }

    private fun setupObserversForUi() {
        viewModel.subjectFetch.observe(this) { launchAlertIfPersonFetchFailedOrFinish(it) }
        viewModel.exitForm.observe(this, LiveDataEventWithContentObserver { exitFormArgs ->
            exitFormArgs?.let { openExitForm.launch(it) }
        })
    }

    private fun launchAlertIfPersonFetchFailedOrFinish(subjectSource: SubjectFetchResult.SubjectSource) {
        when (subjectSource) {
            NOT_FOUND_IN_LOCAL_AND_REMOTE -> showAlert.launch(
                AlertType.GUID_NOT_FOUND_ONLINE.toAlertConfig().toArgs()
            )
            NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR -> showAlert.launch(
                AlertType.GUID_NOT_FOUND_OFFLINE.toAlertConfig().toArgs()
            )
            else -> {
                setResultAndFinish(FetchGUIDResponse(true))
            }
        }
    }

    private fun tryToFetchGuid() {
        viewModel.fetchGuid(fetchGuidRequest.projectId, fetchGuidRequest.verifyGuid)
    }

    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, coreResponse)
    }
}
