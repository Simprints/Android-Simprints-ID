package com.simprints.fingerprint.activities.connect.issues.ota

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.databinding.FragmentOtaBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * Ota (stands for: Over The Air) fragment is used to handle/send
 * Over The Air updates to the fingerprint scanner, have a look at the
 * readme for more details - /connect/README.md
 */
@AndroidEntryPoint
class OtaFragment : FingerprintFragment() {

    @Inject
    lateinit var timeHelper: FingerprintTimeHelper
    @Inject
    lateinit var sessionManager: FingerprintSessionEventsManager

    private val viewModel: OtaViewModel by viewModels()
    private val connectScannerViewModel: ConnectScannerViewModel by viewModels()
    private val binding by viewBinding(FragmentOtaBinding::bind)

    private val args: OtaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_ota, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(
            AlertScreenEventWithScannerIssue(
                timeHelper.now(),
                ConnectScannerIssue.Ota(args.otaFragmentRequest)
            )
        )

        listenForProgress()
        listenForCompleteEvent()
        listenForRecoveryEvent()
        listenForFailedEvent()

        // If the user is coming for a retry, no need to wait for the button press
        if (args.otaFragmentRequest.currentRetryAttempt == 0) {
            initStartUpdateButton()
        } else {
            adjustUiAndStartUpdate()
        }
    }

    private fun setTextInLayout() {
        binding.apply {
            otaTitleTextView.text = getString(R.string.ota_title)
            otaInstructionsTextView.text = getString(R.string.ota_instructions)
            startUpdateButton.text = getString(R.string.start_update)
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
                when (val retry = args.otaFragmentRequest.currentRetryAttempt) {
                    0 -> getString(R.string.updating)
                    else -> String.format(
                        requireActivity().getString(R.string.updating_attempt),
                        "${retry + 1}", "${OtaViewModel.MAX_RETRY_ATTEMPTS + 1}"
                    )
                }
            startUpdateButton.visibility = View.INVISIBLE
            startUpdateButton.isEnabled = false
        }

        connectScannerViewModel.disableBackButton()
        viewModel.startOta(
            args.otaFragmentRequest.availableOtas,
            args.otaFragmentRequest.currentRetryAttempt
        )
    }

    private fun listenForProgress() {
        viewModel.progress.fragmentObserveWith {
            binding.otaProgressBar.progress = (it * 100f).toInt()
        }
    }

    private fun listenForRecoveryEvent() {
        viewModel.otaRecovery.fragmentObserveEventWith {
            findNavController().navigate(
                OtaFragmentDirections.actionOtaFragmentToOtaRecoveryFragment(
                    it
                )
            )
        }
    }

    private fun listenForFailedEvent() {
        viewModel.otaFailed.fragmentObserveEventWith {
            findNavController().navigate(
                OtaFragmentDirections.actionOtaFragmentToOtaFailedFragment(
                    it
                )
            )
        }
    }

    private fun listenForCompleteEvent() {
        viewModel.otaComplete.fragmentObserveEventWith {
            binding.otaStatusTextView.text = getString(R.string.update_complete)
            timeHelper.newTimer().schedule(FINISHED_TIME_DELAY_MS) {
                requireActivity().runOnUiThread { retryConnectAndFinishFragment() }
            }
        }
    }

    private fun retryConnectAndFinishFragment() {
        connectScannerViewModel.retryConnect()
        findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToConnectScannerMainFragment())
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
