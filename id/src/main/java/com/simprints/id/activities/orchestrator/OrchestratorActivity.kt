package com.simprints.id.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.fromDomainToModuleApi
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.Companion.BUNDLE_KEY as APP_REQUEST_BUNDLE_KEY

class OrchestratorActivity : AppCompatActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var orchestratorViewModelFactory: OrchestratorViewModelFactory
    lateinit var appRequest: AppRequest

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    private var newActivity = true

    private val observerForNextStep = Observer<Step?> {
        it?.let {
            with(Intent().setClassName(packageName, it.activityName)) {
                putExtra(it.bundleKey, it.request.fromDomainToModuleApi())
                startActivityForResult(this, it.requestCode)
            }
        }
    }

    private val observerForFinalResponse = Observer<IAppResponse?> {
        it?.let {
            setResult(RESULT_OK, Intent().apply {
                putExtra(IAppResponse.BUNDLE_KEY, it)
            })
            finish()
        }
    }

    private val vm: OrchestratorViewModel by lazy {
        ViewModelProvider(this, orchestratorViewModelFactory).get(OrchestratorViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        with((application as Application)) {
            createOrchestratorComponent()
            orchestratorComponent.inject(this@OrchestratorActivity)
        }
        super.onCreate(savedInstanceState)

        appRequest = this.intent.extras?.getParcelable(APP_REQUEST_BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        vm.startModalityFlow(appRequest)
        syncManager.scheduleBackgroundSyncs()
        imageUpSyncScheduler.scheduleImageUpSync()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        vm.saveState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        vm.restoreState()
        newActivity = false
    }

    override fun onResume() {
        super.onResume()
        vm.ongoingStep.observe(this, observerForNextStep)
        vm.appResponse.observe(this, observerForFinalResponse)

        if (newActivity) {
            vm.clearState()
        }
        newActivity = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vm.onModalStepRequestDone(appRequest, requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.saveState()
    }

}
