package com.simprints.id.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.fromDomainToModuleApi
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
import com.simprints.id.tools.time.TimeHelper
import com.simprints.moduleapi.app.responses.IAppResponse
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.Companion.BUNDLE_KEY as APP_REQUEST_BUNDLE_KEY

class OrchestratorActivity : BaseSplitActivity() {

    @Inject lateinit var orchestratorViewModelFactory: OrchestratorViewModelFactory
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var subjectsSyncManager: SubjectsSyncManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper

    lateinit var appRequest: AppRequest

    private var newActivity = true

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
        setContentView(R.layout.splash_screen)

        appRequest = this.intent.extras?.getParcelable(APP_REQUEST_BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        runBlocking {
            vm.startModalityFlow(appRequest)
        }
        scheduleAndStartSyncIfNecessary()
    }

    private fun scheduleAndStartSyncIfNecessary() {
        if (preferencesManager.subjectsDownSyncSetting == SubjectsDownSyncSetting.EXTRA) {
            subjectsSyncManager.sync()
        }
        syncManager.scheduleBackgroundSyncs()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        vm.saveState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
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
