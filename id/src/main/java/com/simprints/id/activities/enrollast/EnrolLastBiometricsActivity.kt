package com.simprints.id.activities.enrollast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.callout.IdentificationCalloutEvent
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.response.CoreEnrolLastBiometricsResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class EnrolLastBiometricsActivity : AppCompatActivity() {

//    @Inject
//    lateinit var hotCache: HotCache

    @Inject
    lateinit var enrolmentHelper: EnrolmentHelper

    @Inject
    lateinit var timeHelper: TimeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        injectDependencies()
        Timber.d("EnrolLastBiometrics started")

        lifecycleScope.launchWhenCreated {
            try {
//                val steps = hotCache.load()
//                val request = hotCache.appRequest as AppRequest.AppEnrolLastBiometricsRequest
//                val person = enrolmentHelper.buildPerson(
//                    request.projectId,
//                    request.userId,
//                    request.moduleId,
//                    getCaptureResponse<FingerprintCaptureResponse>(steps),
//                    getCaptureResponse<FaceCaptureResponse>(steps),
//                    timeHelper)
//
//                enrolmentHelper.enrol(person)
//                sendOkResult(person.patientId)
            } catch (t: Throwable) {
                Timber.d(t)
                AlertActivityHelper.launchAlert(this@EnrolLastBiometricsActivity, AlertType.ENROLMENT_LAST_BIOMETRIC_FAILED)
            }
        }
    }

    private inline fun <reified T> getCaptureResponse(steps: List<Step>) =
        steps.firstOrNull { it.getResult() is T }?.getResult() as T?


    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun sendOkResult(newSubjectId: String) {
        val response = CoreEnrolLastBiometricsResponse(newSubjectId)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, response)
        })

        Timber.d("EnrolLastBiometrics done")
        finish()
    }
}
