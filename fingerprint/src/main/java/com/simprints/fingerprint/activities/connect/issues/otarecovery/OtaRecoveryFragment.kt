package com.simprints.fingerprint.activities.connect.issues.otarecovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import kotlinx.android.synthetic.main.fragment_ota_recovery.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class OtaRecoveryFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val resourceHelper: FingerprintAndroidResourcesHelper by inject()

    private val viewModel: OtaRecoveryViewModel by viewModel()

    private val args: OtaRecoveryFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_ota_recovery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        tryAgainButton.setOnClickListener { viewModel.handleTryAgainPressed() }

        viewModel.isConnectionSuccess.fragmentObserveEventWith { connectionSuccessful ->
            if (connectionSuccessful) retryOta() else goToOtaFailed()
        }
    }

    private fun retryOta() {
        findNavController().navigate(OtaRecoveryFragmentDirections.actionOtaRecoveryFragmentToOtaFragment(
            OtaFragmentRequest(args.otaRecoveryFragmentRequest.remainingOtas,
                args.otaRecoveryFragmentRequest.currentRetryAttempt + 1)
        ))
    }

    private fun goToOtaFailed() {
        findNavController().navigate(OtaRecoveryFragmentDirections.actionOtaRecoveryFragmentToOtaFailedFragment())
    }

    private fun setTextInLayout() {
        with(resourceHelper) {
            otaRecoveryInstructionsTextView.text = when (args.otaRecoveryFragmentRequest.recoveryStrategy) {
                OtaRecoveryStrategy.UserActionRequired.HardReset -> "Press and hold power button for 7 seconds"
                OtaRecoveryStrategy.UserActionRequired.SoftReset -> "Turn scanner off and on again"
            }
        }
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
