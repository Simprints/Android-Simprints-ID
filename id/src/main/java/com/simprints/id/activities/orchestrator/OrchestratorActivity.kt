package com.simprints.id.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.fromDomainToModuleApi
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.Companion.BUNDLE_KEY as APP_REQUEST_BUNDLE_KEY
import com.simprints.moduleapi.app.responses.IAppResponse.Companion.BUNDLE_KEY as APP_RESPONSE_BUNDLE_KEY

class OrchestratorActivity : AppCompatActivity() {

    @Inject lateinit var orchestratorViewModelFactory: OrchestratorViewModelFactory
    lateinit var appRequest: AppRequest

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper
    @Inject lateinit var timeHelper: TimeHelper

    private val vm: OrchestratorViewModel by lazy {
        ViewModelProviders.of(this, orchestratorViewModelFactory).get(OrchestratorViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as Application).component.inject(this)
        super.onCreate(savedInstanceState)

        appRequest = this.intent.extras?.getParcelable<AppRequest>(APP_REQUEST_BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        vm.nextActivity.observe(this, Observer {
            with(Intent().setClassName(packageName, it.activityName)) {
                putExtra(it.bundleKey, it.request.fromDomainToModuleApi())
                startActivityForResult(this, it.requestCode)
            }
        })

        vm.appResponse.observe(this, Observer {
            setResult(RESULT_OK, Intent().apply {
                putExtra(APP_RESPONSE_BUNDLE_KEY, it)
            })
            finish()
        })

        vm.start(appRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vm.onModalStepRequestDone(requestCode, resultCode, data)
    }
}
