package com.simprints.id.activities.enrollast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.id.R
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FACE
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FINGERPRINT
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class EnrolLastBiometricsActivity : BaseSplitActivity() {

    private lateinit var enrolLastBiometricsRequest: EnrolLastBiometricsRequest

    private val vm: EnrolLastBiometricsViewModel by viewModels()

    private val showAlert = registerForActivityResult(ShowAlertWrapper()) {
        sendOkResult(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        Simber.d("EnrolLastBiometrics started")

        enrolLastBiometricsRequest = intent.extras?.getParcelable(CORE_STEP_BUNDLE)
            ?: throw InvalidAppRequest()

        observeViewState()
        processRequest()
    }

    private fun processRequest() {
        lifecycleScope.launchWhenCreated {
            vm.processEnrolLastBiometricsRequest(enrolLastBiometricsRequest)
        }
    }

    private fun observeViewState() {
        vm.getViewStateLiveData().observe(this) { state ->
            if (state is ViewState.Success) {
                Toast.makeText(
                    this,
                    getString(IDR.string.enrol_last_biometrics_success),
                    Toast.LENGTH_LONG
                ).show()
                sendOkResult(state.newGuid)
            } else {
                lifecycleScope.launch {
                    val customMessage = vm.getAvailabilityModalities()
                        .let {
                            when {
                                it.size >= 2 -> IDR.string.enrol_last_biometrics_alert_message_all_param
                                it.contains(FACE) -> IDR.string.enrol_last_biometrics_alert_message_fingerprint_param
                                it.contains(FINGERPRINT) -> IDR.string.enrol_last_biometrics_alert_message_fingerprint_param
                                else -> IDR.string.enrol_last_biometrics_alert_message_all_param
                            }
                        }
                        .let { getString(it) }
                        .let { getString(IDR.string.enrol_last_biometrics_alert_message, it) }

                    showAlert.launch(AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED.toAlertConfig(customMessage).toArgs())
                }
            }
        }
    }

    private fun sendOkResult(newSubjectId: String?) {
        Simber.d("EnrolLastBiometrics done")
        val response = EnrolLastBiometricsResponse(newSubjectId)
        setResultAndFinish(response)
    }

    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, coreResponse)
    }


    sealed class ViewState {
        class Success(val newGuid: String) : ViewState()
        object Failed : ViewState()
    }
}
