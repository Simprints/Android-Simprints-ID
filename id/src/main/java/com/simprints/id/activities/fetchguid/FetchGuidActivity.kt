package com.simprints.id.activities.fetchguid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.CLOSE
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.TRY_AGAIN
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.requests.FetchGUIDRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.FetchGUIDResponse
import javax.inject.Inject

class FetchGuidActivity : BaseSplitActivity() {

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
        viewModel.subjectFetch.observe(this, Observer {
            launchAlertIfPersonFetchFailedOrFinish(it)
        })
    }

    private fun launchAlertIfPersonFetchFailedOrFinish(subjectSource: SubjectFetchResult.SubjectSource) {
        when(subjectSource) {
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
            CLOSE -> {
                setResultAndFinish(FetchGUIDResponse(false))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tryToGetAlertActResponseAndHandleAction(data) ?: getCoreResponseAndFinish(data)
    }

    private fun tryToGetAlertActResponseAndHandleAction(data: Intent?) =
        data?.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)?.let {
            handleAlertButtonAction(it.buttonAction)
        }

    private fun getCoreResponseAndFinish(data: Intent?) =
        data?.getParcelableExtra<CoreResponse>(CORE_STEP_BUNDLE)?.let {
            setResultAndFinish(it)
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

    override fun onBackPressed() {}
}
