package com.simprints.id.activities.fetchguid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.R
import com.simprints.id.data.db.PersonSource
import com.simprints.id.data.db.PersonSource.NOT_FOUND_IN_LOCAL_AND_REMOTE
import com.simprints.id.data.db.PersonSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest

class FetchGuidActivity : AppCompatActivity() {

    private lateinit var fetchGuidRequest: FetchGUIDRequest
    private lateinit var viewModel: FetchGuidViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_guid)

        fetchGuidRequest = intent.extras?.getParcelable(CoreResponse.VERIFY_STEP_BUNDLE) ?: throw InvalidAppRequest()

        viewModel = ViewModelProviders.of(this).get(FetchGuidViewModel::class.java)

        viewModel.fetchGuid(fetchGuidRequest.projectId, fetchGuidRequest.verifyGuid)

        setupObserversForUi()
    }

    private fun setupObserversForUi() {
        viewModel.personFetch.observe(this, Observer {
            launchAlertIfPersonFetchFailed(it)
        })
    }

    private fun launchAlertIfPersonFetchFailed(personSource: PersonSource) {
        when(personSource) {
            NOT_FOUND_IN_LOCAL_AND_REMOTE -> TODO("Launch dismissible alert")
            NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR -> TODO("Launch try again alert")
            else -> {  }
        }
    }
}
