package com.simprints.id.activities.enrollast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.responsebuilders.AppResponseBuilderForEnrol
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.response.CoreEnrolLastBiometricsResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class EnrolLastBiometricsActivity : AppCompatActivity() {

    @Inject
    lateinit var hotCache: HotCache

    @Inject
    lateinit var enrolmentHelper: EnrolmentHelper

    @Inject
    lateinit var sessionRepository: SessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        injectDependencies()
        Timber.d("EnrolLastBiometrics started")

        lifecycleScope.launchWhenCreated {
            if (isCurrentSessionAnIdentification()) {
                val steps = hotCache.load()
                val request = hotCache.appRequest as AppRequest.AppRequestFlow
                val person = enrolmentHelper.buildPerson(request, fingerprintResponse, faceResponse, timeHelper)
                enrolmentHelper.enrol(person)
            } else {
                AlertActivityHelper.launchAlert(this@EnrolLastBiometricsActivity, AlertType.INVALID_ACTION_INTENT)
            }
        }
    }

    private fun enrolLastBiometrics(steps: List<Step>) {

    }

    private suspend fun isCurrentSessionAnIdentification(): Boolean {
        val currentSession = sessionRepository.getCurrentSession()
        currentSession.getEvents().filterIsInstance(IdentificationCalloutEvent::class.java).isNotEmpty()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun sendOkResult(newSubjectId: UUID) {
        val response = CoreEnrolLastBiometricsResponse(newSubjectId)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, response)
        })

        Timber.d("EnrolLastBiometrics done")
        finish()
    }
}
