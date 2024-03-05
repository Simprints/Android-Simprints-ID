package com.simprints.feature.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.databinding.ActivityOrchestratorBinding
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
    lateinit var orchestratorCache: OrchestratorCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.orchestrationHost.handleResult<AppResult>(
            this,
            R.id.orchestratorRootFragment
        ) { result ->
            setResult(result.resultCode, Intent().putExtras(result.extras))

            orchestratorCache.isExecuting.set(false)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (orchestratorCache.isExecuting.compareAndSet(false, true))  {
            val action = intent.action.orEmpty()
            val extras = intent.extras ?: bundleOf()

            findNavController(R.id.orchestrationHost).setGraph(
                R.navigation.graph_orchestration,
                OrchestratorFragmentArgs(action, extras).toBundle()
            )
        } else {
            Simber.e("Orchestrator already executing, finishing with RESULT_CANCELED")
            finish()
        }
    }

}
