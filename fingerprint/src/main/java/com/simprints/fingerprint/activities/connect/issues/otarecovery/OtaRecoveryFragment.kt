package com.simprints.fingerprint.activities.connect.issues.otarecovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import kotlinx.android.synthetic.main.fragment_ota_recovery.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class OtaRecoveryFragment : FingerprintFragment() {

    private val timeHelper: FingerprintTimeHelper by inject()
    private val sessionManager: FingerprintSessionEventsManager by inject()

    private val viewModel: OtaRecoveryViewModel by viewModel()

    private val args: OtaRecoveryFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_ota_recovery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(AlertScreenEventWithScannerIssue(timeHelper.now(), ConnectScannerIssue.OtaRecovery(args.otaRecoveryFragmentRequest)))

        setupTryAgainButton()
        viewModel.isConnectionSuccess.fragmentObserveEventWith { connectionSuccessful ->
            if (connectionSuccessful) retryOta() else goToOtaFailed()
        }
    }

    private fun setTextInLayout() {
        otaRecoveryTitleTextView.text = getString(R.string.ota_recovery_title)
        tryAgainButton.text = getString(R.string.try_again_label)
        otaRecoveryInstructionsTextView.text = getString(when (args.otaRecoveryFragmentRequest.recoveryStrategy) {
            OtaRecoveryStrategy.HARD_RESET -> R.string.ota_recovery_hard_reset
            OtaRecoveryStrategy.SOFT_RESET,
            OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY -> R.string.ota_recovery_soft_reset
        })
    }

    private fun setupTryAgainButton() {
        tryAgainButton.setOnClickListener {
            tryAgainButton.isEnabled = false
            tryAgainButton.visibility = View.INVISIBLE
            otaRecoveryProgressBar.visibility = View.VISIBLE
            viewModel.handleTryAgainPressed()
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
}
