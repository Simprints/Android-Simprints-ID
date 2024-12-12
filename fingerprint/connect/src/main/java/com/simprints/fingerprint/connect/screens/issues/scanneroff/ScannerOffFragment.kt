package com.simprints.fingerprint.connect.screens.issues.scanneroff

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentScannerOffBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ScannerOffFragment : Fragment(R.layout.fragment_scanner_off) {
    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()
    private val binding by viewBinding(FragmentScannerOffBinding::bind)
    private val args: ScannerOffFragmentArgs by navArgs()

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        screenReporter.reportScannerOff()

        binding.tryAgainButton.setOnClickListener {
            connectScannerViewModel.startRetryingToConnect()
        }
        args.scannerId?.let {
            binding.couldNotConnectTextView.paintFlags =
                binding.couldNotConnectTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            binding.couldNotConnectTextView.text = getString(IDR.string.fingerprint_connect_not_my_scanner, it)
            binding.couldNotConnectTextView.setOnClickListener { connectScannerViewModel.handleIncorrectScanner() }
            binding.couldNotConnectTextView.visibility = View.VISIBLE
        }

        initViewModelObservation()
        connectScannerViewModel.startRetryingToConnect()
    }

    private fun initViewModelObservation() {
        connectScannerViewModel.isConnecting.observe(viewLifecycleOwner) { isConnecting ->
            if (isConnecting) {
                setupConnectingMode()
            } else {
                setupTryAgainMode()
            }
        }
        connectScannerViewModel.scannerConnected.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { success ->
                if (success) {
                    handleScannerConnectedEvent()
                }
            },
        )
        connectScannerViewModel.showScannerIssueScreen.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                connectScannerViewModel.resetConnectionState()
            },
        )
    }

    private fun handleScannerConnectedEvent() {
        binding.apply {
            scannerOffProgressBar.visibility = View.INVISIBLE
            couldNotConnectTextView.visibility = View.INVISIBLE
            tryAgainButton.visibility = View.VISIBLE
            tryAgainButton.isEnabled = false
            tryAgainButton.text = getString(IDR.string.fingerprint_connect_scanner_on)
            tryAgainButton.setBackgroundColor(resources.getColor(IDR.color.simprints_green, null))
        }

        lifecycleScope.launch {
            delay(FINISHED_TIME_DELAY_MS)
            connectScannerViewModel.finishConnectionFlow(true)
        }
    }

    private fun setupConnectingMode() {
        binding.apply {
            scannerOffProgressBar.visibility = View.VISIBLE
            tryAgainButton.visibility = View.INVISIBLE
            tryAgainButton.isEnabled = false
        }
    }

    private fun setupTryAgainMode() {
        binding.apply {
            scannerOffProgressBar.visibility = View.INVISIBLE
            tryAgainButton.visibility = View.VISIBLE
            tryAgainButton.isEnabled = true
        }
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
