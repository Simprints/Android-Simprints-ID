package com.simprints.fingerprint.connect.screens.ota.recovery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentOtaRecoveryBinding
import com.simprints.fingerprint.connect.screens.ota.OtaFragmentParams
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

/**
 * This fragment is show when an Over The Air update fails, and a reset is required
 * have a look at the readme for more details - /connect/README.md
 */
@AndroidEntryPoint
internal class OtaRecoveryFragment : Fragment(R.layout.fragment_ota_recovery) {

    private val viewModel: OtaRecoveryViewModel by viewModels()
    private val binding by viewBinding(FragmentOtaRecoveryBinding::bind)
    private val args: OtaRecoveryFragmentArgs by navArgs()

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenReporter.reportOtaRecovery()

        setRecoveryStrategyInstructions()
        setupTryAgainButton()

        viewModel.isConnectionSuccess.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { connectionSuccessful ->
            if (connectionSuccessful) retryOta() else goToOtaFailed()
        })
    }

    private fun setRecoveryStrategyInstructions() {
        binding.otaRecoveryInstructionsTextView.setText(
            when (args.params.recoveryStrategy) {
                OtaRecoveryStrategy.HARD_RESET -> IDR.string.fingerprint_connect_ota_recovery_hard_reset
                OtaRecoveryStrategy.SOFT_RESET,
                OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY -> IDR.string.fingerprint_connect_ota_recovery_soft_reset
            }
        )
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
        findNavController().navigateSafely(
            this,
            OtaRecoveryFragmentDirections.actionOtaRecoveryFragmentToOtaFragment(OtaFragmentParams(
                fingerprintSDK = args.params.fingerprintSDK,
                availableOtas = args.params.remainingOtas,
                currentRetryAttempt = args.params.currentRetryAttempt + 1
            ))
        )
    }

    private fun goToOtaFailed() {
        findNavController().navigateSafely(
            this,
            OtaRecoveryFragmentDirections.actionOtaRecoveryFragmentToOtaFailedFragment(null)
        )
    }
}
