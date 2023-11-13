package com.simprints.fingerprint.connect.screens.ota

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentOtaBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class OtaFragment : Fragment(R.layout.fragment_ota) {


    private val viewModel: OtaViewModel by viewModels()
    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()
    private val binding by viewBinding(FragmentOtaBinding::bind)

    private val args: OtaFragmentArgs by navArgs()

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenReporter.reportOta()

        listenForProgress()
        listenForCompleteEvent()
        listenForRecoveryEvent()
        listenForFailedEvent()

        // If the user is coming for a retry, no need to wait for the button press
        if (args.params.currentRetryAttempt == 0) {
            initStartUpdateButton()
        } else {
            adjustUiAndStartUpdate()
        }
    }

    private fun initStartUpdateButton() {
        binding.startUpdateButton.setOnClickListener {
            adjustUiAndStartUpdate()
        }
    }

    private fun adjustUiAndStartUpdate() {
        binding.apply {
            otaProgressBar.visibility = View.VISIBLE
            otaStatusTextView.visibility = View.VISIBLE
            otaStatusTextView.text =
                when (val retry = args.params.currentRetryAttempt) {
                    0 -> getString(IDR.string.updating)
                    else -> String.format(
                        requireActivity().getString(IDR.string.updating_attempt),
                        "${retry + 1}", "${OtaViewModel.MAX_RETRY_ATTEMPTS + 1}"
                    )
                }
            startUpdateButton.visibility = View.INVISIBLE
            startUpdateButton.isEnabled = false
        }

        connectScannerViewModel.disableBackButton()
        viewModel.startOta(
            args.params.availableOtas,
            args.params.currentRetryAttempt
        )
    }

    private fun listenForProgress() {
        viewModel.progress.observe(viewLifecycleOwner) {
            binding.otaProgressBar.progress = (it * 100f).toInt()
        }
    }

    private fun listenForRecoveryEvent() {
        viewModel.otaRecovery.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToOtaRecoveryFragment(it))
        })
    }

    private fun listenForFailedEvent() {
        viewModel.otaFailed.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToOtaFailedFragment(it))
        })
    }

    private fun listenForCompleteEvent() {
        viewModel.otaComplete.observe(viewLifecycleOwner, LiveDataEventObserver {
            binding.otaStatusTextView.text = getString(IDR.string.update_complete)

            lifecycleScope.launch {
                delay(FINISHED_TIME_DELAY_MS)
                retryConnectAndFinishFragment()
            }
        })
    }

    private fun retryConnectAndFinishFragment() {
        connectScannerViewModel.connect()
        findNavController().navigate(
            OtaFragmentDirections.actionOtaFragmentToConnectProgressFragment(),
            navOptions { popUpTo(R.id.connectProgressFragment) }
        )
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
