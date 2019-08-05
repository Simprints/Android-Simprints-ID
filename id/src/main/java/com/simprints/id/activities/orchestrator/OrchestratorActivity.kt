package com.simprints.id.activities.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.activities.orchestrator.di.OrchestratorComponentInjector
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

class OrchestratorViewModelFactory(val appRequest: AppRequest,
                                   val orchestratorManager: OrchestratorManager,
                                   val sessionEventsManager: SessionEventsManager,
                                   val syncSchedulerHelper: SyncSchedulerHelper,
                                   val timeHelper: TimeHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        OrchestratorViewModel(appRequest, orchestratorManager, sessionEventsManager, syncSchedulerHelper, timeHelper) as T
}


class OrchestratorActivity : AppCompatActivity() {

    lateinit var appRequest: AppRequest
    @Inject lateinit var orchestratorManager: OrchestratorManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper
    @Inject lateinit var timeHelper: TimeHelper

    private val vm: OrchestratorViewModel by lazy {
        ViewModelProviders.of(this, OrchestratorViewModelFactory(
            appRequest,
            orchestratorManager,
            sessionEventsManager,
            syncSchedulerHelper,
            timeHelper))
            .get(OrchestratorViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        OrchestratorComponentInjector.inject(this)
        super.onCreate(savedInstanceState)

        appRequest = this.intent.extras?.getParcelable<AppRequest>(AppRequest.BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        vm.nextActivity.observe(this, Observer {
            startActivityForResult(it.intent, it.requestCode)
        })

        vm.appResponse.observe(this, Observer {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IAppResponse.BUNDLE_KEY, it)
            })
            finish()
        })

        vm.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vm.onModalStepRequestDone(requestCode, resultCode, data)
    }

    fun setCancelResultAndFinish() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        OrchestratorComponentInjector.component = null
    }
}
