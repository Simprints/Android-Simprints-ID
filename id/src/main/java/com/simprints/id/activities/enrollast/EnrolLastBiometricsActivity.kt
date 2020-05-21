package com.simprints.id.activities.enrollast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import javax.inject.Inject

class EnrolLastBiometricsActivity : AppCompatActivity() {

    @Inject
    lateinit var enrolmentHelper: EnrolmentHelper

    @Inject
    lateinit var timeHelper: TimeHelper

    private lateinit var enrolLastBiometricsRequest: EnrolLastBiometricsRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        injectDependencies()
        Timber.d("EnrolLastBiometrics started")

        enrolLastBiometricsRequest = intent.extras?.getParcelable(CORE_STEP_BUNDLE)
            ?: throw InvalidAppRequest()

        lifecycleScope.launchWhenCreated {
            with(enrolLastBiometricsRequest) {
                try {
                    val steps = enrolLastBiometricsRequest.steps
                    if(steps.firstOrNull { it.request is EnrolLastBiometricsRequest } != null) {
                        throw Throwable("EnrolLastBiometricsActivity already happened in this session")
                    }

                    val person = enrolmentHelper.buildPerson(
                        projectId,
                        userId,
                        moduleId,
                        getCaptureResponse<FingerprintCaptureResponse>(steps),
                        getCaptureResponse<FaceCaptureResponse>(steps),
                        timeHelper)

                    enrolmentHelper.enrol(person)
                    sendOkResult(person.patientId)
                } catch (t: Throwable) {
                    Timber.d(t)
                    AlertActivityHelper.launchAlert(this@EnrolLastBiometricsActivity, AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED)
                }
            }
        }
    }

    private inline fun <reified T> getCaptureResponse(steps: List<Step>) =
        steps.firstOrNull { it.getResult() is T }?.getResult() as T?

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        sendOkResult(null)
    }


    private fun sendOkResult(newSubjectId: String?) {
        Timber.d("EnrolLastBiometrics done")
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
}
