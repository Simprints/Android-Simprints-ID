package com.simprints.fingerprint.activities.connect.issues.otarecovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.databinding.FragmentOtaRecoveryBinding
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * This fragment is show when an Over The Air update fails, and a reset is required
 * have a look at the readme for more details - /connect/README.md
 */
class OtaRecoveryFragment : FingerprintFragment() {

    private val timeHelper: FingerprintTimeHelper by inject()
    private val sessionManager: FingerprintSessionEventsManager by inject()

    private val viewModel: OtaRecoveryViewModel by viewModel()
    private val binding by viewBinding(FragmentOtaRecoveryBinding::bind)

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
        binding.otaRecoveryTitleTextView.text = getString(R.string.ota_recovery_title)
        binding.tryAgainButton.text = getString(R.string.try_again_label)
        val instructionResourceId = when (args.otaRecoveryFragmentRequest.recoveryStrategy) {
            OtaRecoveryStrategy.HARD_RESET -> R.string.ota_recovery_hard_reset
            OtaRecoveryStrategy.SOFT_RESET,
            OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY -> R.string.ota_recovery_soft_reset
        }
        binding.otaRecoveryInstructionsTextView.text = getString(instructionResourceId)
    }

    private fun setupTryAgainButton() {
        binding.tryAgainButton.setOnClickListener {
            binding.tryAgainButton.isEnabled = false
            binding.tryAgainButton.visibility = View.INVISIBLE
            binding.otaRecoveryProgressBar.visibility = View.VISIBLE
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
