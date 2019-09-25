package com.simprints.id.activities.fetchguid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.*
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fetchguid.result.FetchGuidResult.Companion.RESULT_CODE_FETCH_GUID
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.NOT_FOUND_IN_LOCAL_AND_REMOTE
import com.simprints.id.data.db.PersonFetchResult.PersonSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.response.FetchGUIDResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import javax.inject.Inject

class FetchGuidActivity : AppCompatActivity() {

    private lateinit var fetchGuidRequest: FetchGUIDRequest
    private lateinit var viewModel: FetchGuidViewModel

    @Inject lateinit var fetchGuidViewModelFactory: FetchGuidViewModelFactory
    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_guid)

        fetchGuidRequest = intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()

        injectDependencies()
        viewModel = ViewModelProvider(this, fetchGuidViewModelFactory).get(FetchGuidViewModel::class.java)

        tryToFetchGuid()

        setupObserversForUi()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setupObserversForUi() {
        viewModel.personFetch.observe(this, Observer {
            launchAlertIfPersonFetchFailedOrFinish(it)
        })
    }

    private fun launchAlertIfPersonFetchFailedOrFinish(personSource: PersonFetchResult.PersonSource) {
        when(personSource) {
            NOT_FOUND_IN_LOCAL_AND_REMOTE -> launchAlert(this, AlertType.GUID_NOT_FOUND_ONLINE)
            NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR -> launchAlert(this, AlertType.GUID_NOT_FOUND_OFFLINE)
            else -> {
                setResultAndFinish(FetchGUIDResponse(true))
            }
        }
    }

    private fun handleAlertButtonAction(alertButtonAction: AlertActResponse.ButtonAction) {
        when (alertButtonAction) {
            TRY_AGAIN -> {
                tryToFetchGuid()
            }
            BACK -> {
                startExitFormActivity()
            }
            CLOSE -> {
                setResultAndFinish(FetchGUIDResponse(false))
            }
        }
    }

    private fun startExitFormActivity() {
        if (isSingleModality()) {
            startModalitySpecificExitForm()
        } else {
            startCoreExitFormActivity()
        }
    }
    private fun isSingleModality() = preferencesManager.modalities.size == 1

    private fun startModalitySpecificExitForm() {
        when (preferencesManager.modalities.first()) {
            Modality.FINGER -> startFingerprintExitFormActivity()
            Modality.FACE -> startFaceExitFormActivity()
        }
    }

    private fun startCoreExitFormActivity() {
        startActivityForResult(Intent(this, CoreExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }

    private fun startFingerprintExitFormActivity() {
        startActivityForResult(Intent(this, FingerprintExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }

    private fun startFaceExitFormActivity() {
        startActivityForResult(Intent(this, FaceExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)?.let {
            handleAlertButtonAction(it.buttonAction)
        } ?: data?.getParcelableExtra<CoreResponse>(CORE_STEP_BUNDLE)?.let {
            setResultAndFinish(it)
        }
    }

    private fun tryToFetchGuid() {
        viewModel.fetchGuid(fetchGuidRequest.projectId, fetchGuidRequest.verifyGuid)
    }


    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(RESULT_CODE_FETCH_GUID, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, coreResponse)
    }
}
