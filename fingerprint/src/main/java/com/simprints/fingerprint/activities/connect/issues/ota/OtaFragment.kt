package com.simprints.fingerprint.activities.connect.issues.ota

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import kotlinx.android.synthetic.main.fragment_ota.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.concurrent.schedule

class OtaFragment : FingerprintFragment() {

    private val resourceHelper: FingerprintAndroidResourcesHelper by inject()
    private val timeHelper: FingerprintTimeHelper by inject()
    private val sessionManager: FingerprintSessionEventsManager by inject()

    private val viewModel: OtaViewModel by viewModel()
    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    private val args: OtaFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_ota, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(AlertScreenEventWithScannerIssue(timeHelper.now(), ConnectScannerIssue.Ota(args.otaFragmentRequest)))

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
        with(resourceHelper) {
            otaTitleTextView.text = getString(R.string.ota_title)
            otaInstructionsTextView.text = getString(R.string.ota_instructions)
            startUpdateButton.text = getString(R.string.start_update)
        }
    }

    private fun initStartUpdateButton() {
        startUpdateButton.setOnClickListener {
            adjustUiAndStartUpdate()
        }
    }

    private fun adjustUiAndStartUpdate() {
        otaProgressBar.visibility = View.VISIBLE
        otaStatusTextView.visibility = View.VISIBLE
        otaStatusTextView.text = when (val retry = args.otaFragmentRequest.currentRetryAttempt) {
            0 -> resourceHelper.getString(R.string.updating)
            else -> resourceHelper.getString(R.string.updating_attempt, arrayOf("${retry + 1}", "${OtaViewModel.MAX_RETRY_ATTEMPTS + 1}"))
        }
        startUpdateButton.visibility = View.INVISIBLE
        startUpdateButton.isEnabled = false
        viewModel.startOta(args.otaFragmentRequest.availableOtas, args.otaFragmentRequest.currentRetryAttempt)
    }

    private fun listenForProgress() {
        viewModel.progress.fragmentObserveWith {
            otaProgressBar.progress = (it * 100f).toInt()
        }
    }

    private fun listenForRecoveryEvent() {
        viewModel.otaRecovery.fragmentObserveEventWith {
            findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToOtaRecoveryFragment(it))
        }
    }

    private fun listenForFailedEvent() {
        viewModel.otaFailed.fragmentObserveEventWith {
            findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToOtaFailedFragment())
        }
    }

    private fun listenForCompleteEvent() {
        viewModel.otaComplete.fragmentObserveEventWith {
            otaStatusTextView.text = resourceHelper.getString(R.string.update_complete)
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
