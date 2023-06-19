package com.simprints.feature.alert.intent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.R
import com.simprints.feature.alert.databinding.ActivityAlertWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class AlertWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityAlertWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.alertHostFragment.handleResult<AlertResult>(this, R.id.alertFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(ALERT_RESULT, result) })

            if (result.isBackButtonPress()) {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val args = intent.extras?.getBundle(ALERT_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.alert_host_fragment).setGraph(R.navigation.graph_alert, args)
    }

    companion object {

        internal const val ALERT_ARGS_EXTRA = "alert_args"
        internal const val ALERT_RESULT = "alert_fragment_result"
    }
}
