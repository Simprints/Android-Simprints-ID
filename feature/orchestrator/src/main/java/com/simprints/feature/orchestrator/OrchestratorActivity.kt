package com.simprints.feature.orchestrator

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.orchestrator.databinding.ActivityOrchestratorBinding
import com.simprints.infra.orchestration.data.results.AppResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

private const val KEY_IS_GRAPH_INITIALIZED = "KEY_IS_GRAPH_INITIALIZED"

@AndroidEntryPoint
internal class OrchestratorActivity : BaseActivity() {

    private val binding by viewBinding(ActivityOrchestratorBinding::inflate)

    /**
     * Flag for the navigation graph initialization state. The graph should only be initialized once
     * during the existence of this activity, and the flag tracks graph's state.
     */
    private var isGraphInitialized = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isGraphInitialized = savedInstanceState?.getBoolean(KEY_IS_GRAPH_INITIALIZED) ?: false
        setContentView(binding.root)

        binding.orchestrationHost.handleResult<AppResult>(this, R.id.orchestratorRootFragment) { result ->
            setResult(result.resultCode, Intent().putExtras(result.extras))
            finish()
        }
    }

    override fun onStart() {
        if(!isGraphInitialized) {
            super.onStart()
            val action = intent.action.orEmpty()
            val extras = intent.extras ?: bundleOf()

            findNavController(R.id.orchestrationHost).setGraph(
                R.navigation.graph_orchestration,
                OrchestratorFragmentArgs(action, extras).toBundle()
            )
            isGraphInitialized = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBoolean(KEY_IS_GRAPH_INITIALIZED, isGraphInitialized)
        super.onSaveInstanceState(outState, outPersistentState)
    }
}
