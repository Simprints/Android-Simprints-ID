package com.simprints.feature.orchestrator

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.clientapi.models.ClientApiConstants
import com.simprints.feature.orchestrator.databinding.ActivityOrchestratorBinding
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

    /**
     * Flag for the navigation graph initialization state. The graph should only be initialized once
     * during the existence of this activity, and the flag tracks graph's state.
     */
    private var isGraphInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Simber.d("OrchestratorActivity.onCreate isGraphInitialized=$isGraphInitialized", tag = ORCHESTRATION)

        isGraphInitialized = savedInstanceState?.getBoolean(KEY_IS_GRAPH_INITIALIZED) ?: false
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

        if (activityTracker.isMain(activity = this)) {
            if (!isGraphInitialized) {
                val action = intent.action.orEmpty()
                val extras = intent.extras ?: bundleOf()
                Simber.setUserProperty("Intent_received", action)
                Simber.setUserProperty("Caller", callingPackage.orEmpty())

                // Some co-sync functionality depends on the exact package name of the caller app,
                // e.g. to switch content providers of debug and release variants of the caller app
                extras.putString(ClientApiConstants.CALLER_PACKAGE_NAME, callingPackage)

                findNavController(R.id.orchestrationHost).setGraph(
                    R.navigation.graph_orchestration,
                    OrchestratorFragmentArgs(action, extras).toBundle(),
                )
                isGraphInitialized = true
            }
        } else {
            Simber.i("Orchestrator already executing, finishing with RESULT_CANCELED", tag = ORCHESTRATION)
            finish()
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle,
        outPersistentState: PersistableBundle,
    ) {
        outState.putBoolean(KEY_IS_GRAPH_INITIALIZED, isGraphInitialized)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    companion object {
        private const val KEY_IS_GRAPH_INITIALIZED = "KEY_IS_GRAPH_INITIALIZED"
    }
}
