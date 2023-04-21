package com.simprints.feature.alert.intent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.R
import com.simprints.feature.alert.databinding.ActivityAlertWrapperBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class AlertWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityAlertWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.alertHostFragment.getFragment<Fragment>().childFragmentManager
            .setFragmentResultListener(AlertContract.ALERT_REQUEST, this) { _, d ->
                // Pass the fragment results directly into activity results
                setResult(RESULT_OK, Intent().also {
                    it.putExtra(AlertContract.ALERT_PAYLOAD, AlertContract.getResponsePayload(d))
                    it.putExtra(AlertContract.ALERT_BUTTON_PRESSED, d.getString(AlertContract.ALERT_BUTTON_PRESSED))
                })

                if (AlertContract.hasResponseKey(d, AlertContract.ALERT_BUTTON_PRESSED_BACK)) {
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
    }
}
