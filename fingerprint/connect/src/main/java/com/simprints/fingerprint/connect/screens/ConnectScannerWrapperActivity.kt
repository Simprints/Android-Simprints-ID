package com.simprints.fingerprint.connect.screens

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.fingerprint.connect.FingerprintConnectContract
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.ActivityConnectScannerWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectScannerWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityConnectScannerWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.connectScannerHost.handleResult<Parcelable>(this, R.id.connectScannerControllerFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(FingerprintConnectContract.RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        val args = intent.extras?.getBundle(SCANNER_CONNECT_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.connectScannerHost).setGraph(R.navigation.graph_connect_scanner, args)
    }

    companion object {

        const val SCANNER_CONNECT_ARGS_EXTRA = "scanner_connect_args"
    }
}
