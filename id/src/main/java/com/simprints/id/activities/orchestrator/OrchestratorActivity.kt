package com.simprints.id.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.fromDomainToModuleApi
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.Companion.BUNDLE_KEY as APP_REQUEST_BUNDLE_KEY

@AndroidEntryPoint
class OrchestratorActivity : BaseSplitActivity() {

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    lateinit var appRequest: AppRequest

    private var syncFrequency = SynchronizationConfiguration.Frequency.PERIODICALLY

    private val observerForNextStep = Observer<Step?> {
        it?.let {
            with(Intent().setClassName(packageName, it.activityName)) {
                putExtra(it.bundleKey, it.request.fromDomainToModuleApi())
                startActivityForResult(this, it.requestCode)
                this@OrchestratorActivity.removeAnimationsToNextActivity()
            }
        }
    }

    private val observerForFinalResponse = Observer<IAppResponse?> {
        it?.let {
            if (it is IAppErrorResponse && it.reason == IAppErrorReason.UNEXPECTED_ERROR) {
                AlertActivityHelper.launchAlert(
                    this@OrchestratorActivity,
                    AlertType.UNEXPECTED_ERROR
                )
            } else
                setResult(RESULT_OK, Intent().apply {
                    putExtra(IAppResponse.BUNDLE_KEY, it)
                })
            finish()
        }
    }

    private val vm: OrchestratorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        appRequest = this.intent.extras?.getParcelable(APP_REQUEST_BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        vm.initializeModalityFlow(appRequest)
        if (savedInstanceState == null) {
            vm.startModalityFlow()
            scheduleAndStartSyncIfNecessary()
        } else {
            vm.restoreState()
        }

        fetchData()
    }

    private fun fetchData() {
        vm.syncFrequency.observe(this) {
            syncFrequency = it
            this.scheduleAndStartSyncIfNecessary()
        }
    }

    private fun scheduleAndStartSyncIfNecessary() {
        syncManager.scheduleBackgroundSyncs()
        if (syncFrequency == SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START) {
            eventSyncManager.sync()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        vm.saveState()
    }

    override fun onResume() {
        super.onResume()

        vm.ongoingStep.observe(this, observerForNextStep)
        vm.appResponse.observe(this, observerForFinalResponse)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vm.onModalStepRequestDone(appRequest, requestCode, resultCode, data)
    }
}
