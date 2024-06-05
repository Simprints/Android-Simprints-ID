package com.simprints.feature.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.clientapi.models.ClientApiConstants
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
    lateinit var activityTracker: ExecutionTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(activityTracker)

        setContentView(binding.root)

        binding.orchestrationHost.handleResult<AppResult>(
            this,
            R.id.orchestratorRootFragment
        ) { result ->
            setResult(result.resultCode, Intent().putExtras(result.extras))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        if (activityTracker.isMain(activity = this)) {
            val action = intent.action.orEmpty()
            val extras = intent.extras ?: bundleOf()

            // Some co-sync functionality depends on the exact package name of the caller app,
            // e.g. to switch content providers of debug and release variants of the caller app
            extras.putString(ClientApiConstants.CALLER_PACKAGE_NAME, callingPackage)

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
