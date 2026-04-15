package com.simprints.feature.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.orchestrator.databinding.ActivityOrchestratorBinding
import com.simprints.feature.storage.alert.ShowStorageAlertIfNecessaryUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.results.AppResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class OrchestratorActivity : BaseActivity() {
    private val binding by viewBinding(ActivityOrchestratorBinding::inflate)

    @Inject
    lateinit var activityTracker: ExecutionTracker

    @Inject
    lateinit var showStorageAlertIfNecessary: ShowStorageAlertIfNecessaryUseCase

    /**
     * Flag for the navigation graph initialization state. The graph should only be initialized once
     * while the activity is in memory. Recreation of the activity should result in graph re-init
     */
    private var isGraphInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Simber.d("OrchestratorActivity.onCreate isGraphInitialized=$isGraphInitialized", tag = ORCHESTRATION)

        lifecycle.addObserver(activityTracker)

        setContentView(binding.root)

        binding.orchestrationHost.handleResult<AppResult>(
            this,
            R.id.orchestratorRootFragment,
        ) { result ->
            Simber.d("OrchestratorActivity result code ${result.resultCode}", tag = ORCHESTRATION)

            setResult(result.resultCode, Intent().putExtras(result.extras))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Simber.d("OrchestratorActivity.onStart isGraphInitialized=$isGraphInitialized", tag = ORCHESTRATION)
        showStorageAlertIfNecessary()

        if (activityTracker.isMain(activity = this)) {
            if (!isGraphInitialized) {
                findNavController(R.id.orchestrationHost).setGraph(
                    R.navigation.graph_orchestration,
                )
                isGraphInitialized = true
            }
        } else {
            Simber.i("Orchestrator already executing, finishing with RESULT_CANCELED", tag = ORCHESTRATION)
            finish()
        }
    }

    override fun onLogout(isProjectEnded: Boolean) {
        Simber.d("Logged out during workflow", tag = ORCHESTRATION)
        // This should only happen when device is compromised and state refresh happens during execution of a workflow.
        // Therefore, there is no reason to continue workflow or attempt to preserve any data.
        setResult(RESULT_CANCELED, Intent().putExtras(Bundle()))
        finish()
    }
}
