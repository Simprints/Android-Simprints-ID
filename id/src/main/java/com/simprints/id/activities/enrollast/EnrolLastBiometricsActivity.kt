package com.simprints.id.activities.enrollast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import javax.inject.Inject

class EnrolLastBiometricsActivity : AppCompatActivity() {

    @Inject
    lateinit var enrolmentHelper: EnrolmentHelper

    @Inject
    lateinit var timeHelper: TimeHelper

    @Inject
    lateinit var viewModelFactory: EnrolLastBiometricsViewModelFactory

    @Inject
    lateinit var androidResourcesHelper: AndroidResourcesHelper

    private lateinit var enrolLastBiometricsRequest: EnrolLastBiometricsRequest

    private val vm: EnrolLastBiometricsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(EnrolLastBiometricsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        injectDependencies()
        Timber.d("EnrolLastBiometrics started")

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
        vm.getViewStateLiveData().observe(this, Observer {
            if (it is ViewState.Success) {
                Toast.makeText(this, androidResourcesHelper.getString(R.string.enrol_last_biometrics_success), Toast.LENGTH_LONG).show()
                sendOkResult(it.newGuid)
            } else {
                AlertActivityHelper.launchAlert(this@EnrolLastBiometricsActivity, AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED)
            }
        })
    }


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


    sealed class ViewState {
        class Success(val newGuid: String) : ViewState()
        object Failed : ViewState()
    }
}
