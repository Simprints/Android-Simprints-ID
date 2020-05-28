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
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import kotlinx.android.synthetic.main.fragment_ota.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.concurrent.schedule

class OtaFragment : FingerprintFragment() {

    private val resourceHelper: FingerprintAndroidResourcesHelper by inject()
    private val timeHelper: FingerprintTimeHelper by inject()

    private val viewModel: OtaViewModel by viewModel()
    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    private val args: OtaFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_ota, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        initStartUpdateButton()
        listenForProgress()
        listenForCompleteEvent()
        listenForFailedEvent()
    }

    private fun setTextInLayout() {
        with(resourceHelper) {
            // TODO
        }
    }

    private fun initStartUpdateButton() {
        startUpdateButton.setOnClickListener {
            otaProgressBar.visibility = View.VISIBLE
            otaStatusTextView.visibility = View.VISIBLE
            startUpdateButton.visibility = View.INVISIBLE
            startUpdateButton.isEnabled = false
            viewModel.startOta(args.otaFragmentRequest.availableOtas, args.otaFragmentRequest.currentRetryAttempt)
        }
    }

    private fun listenForProgress() {
        viewModel.progress.fragmentObserveWith {
            otaProgressBar.progress = (it * 100f).toInt()
        }
    }

    private fun listenForFailedEvent() {
        viewModel.otaFailedNeedingUserAction.fragmentObserveEventWith {
            findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToOtaRecoveryFragment(it))
        }
    }

    private fun listenForCompleteEvent() {
        viewModel.otaComplete.fragmentObserveEventWith {
            otaStatusTextView.text = "Updated ✓"
            timeHelper.newTimer().schedule(FINISHED_TIME_DELAY_MS) { retryConnectAndFinishFragment() }
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
