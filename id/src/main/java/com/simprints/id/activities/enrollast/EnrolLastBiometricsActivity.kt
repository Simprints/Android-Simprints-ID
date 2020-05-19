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
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
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
                    val person = enrolmentHelper.buildPerson(
                        projectId,
                        userId,
                        moduleId,
                        fingerprintCaptureResponse,
                        faceCaptureResponse,
                        timeHelper)

                    enrolmentHelper.enrol(person)
                    sendOkResult(person.patientId)
                } catch (t: Throwable) {
                    Timber.d(t)
                    AlertActivityHelper.launchAlert(this@EnrolLastBiometricsActivity, AlertType.ENROLMENT_LAST_BIOMETRIC_FAILED)
                }
            }
        }
    }


    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun sendOkResult(newSubjectId: String) {
        val response = EnrolLastBiometricsResponse(newSubjectId)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, response)
        })

        Timber.d("EnrolLastBiometrics done")
        finish()
    }
}
