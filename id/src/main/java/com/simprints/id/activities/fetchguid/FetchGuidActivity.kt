package com.simprints.id.activities.fetchguid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.*
import com.simprints.id.activities.fetchguid.result.FetchGuidResult.Companion.RESULT_CODE_FETCH_GUID
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource.NOT_FOUND_IN_LOCAL_AND_REMOTE
import com.simprints.id.data.db.PersonFetchResult.PersonSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.response.FetchGUIDResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import javax.inject.Inject

class FetchGuidActivity : AppCompatActivity() {

    private lateinit var fetchGuidRequest: FetchGUIDRequest
    private lateinit var viewModel: FetchGuidViewModel

    @Inject lateinit var fetchGuidViewModelFactory: FetchGuidViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_guid)

        fetchGuidRequest = intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()

        injectDependencies()
        viewModel = ViewModelProviders.of(this, fetchGuidViewModelFactory).get(FetchGuidViewModel::class.java)

        viewModel.fetchGuid(fetchGuidRequest.projectId, fetchGuidRequest.verifyGuid)

        setupObserversForUi()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setupObserversForUi() {
        viewModel.personFetch.observe(this, Observer {
            launchAlertIfPersonFetchFailed(it)
        })
    }

    private fun launchAlertIfPersonFetchFailed(personSource: PersonFetchResult.PersonSource) {
        when(personSource) {
            NOT_FOUND_IN_LOCAL_AND_REMOTE -> launchAlert(this, AlertType.GUID_NOT_FOUND_ONLINE)
            NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR -> launchAlert(this, AlertType.GUID_NOT_FOUND_OFFLINE)
            else -> {
                setResultAndFinish(true)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)?.let {
            when(it.buttonAction) {
                TRY_AGAIN -> viewModel.fetchGuid(fetchGuidRequest.projectId, fetchGuidRequest.verifyGuid)
                BACK -> {
                    //ExitFormActivity?
                }
                CLOSE -> {
                    setResultAndFinish(false)
                }
            }
        }
    }

    private fun setResultAndFinish(isGuidFound: Boolean) {
        setResult(RESULT_CODE_FETCH_GUID, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, FetchGUIDResponse(isGuidFound))
        })
        finish()
    }
}
