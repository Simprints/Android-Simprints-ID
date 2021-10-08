package com.simprints.fingerprint.activities.connect.issues.otafailed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.databinding.FragmentOtaFailedBinding
import com.simprints.fingerprint.tools.livedata.postEvent
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * This fragment is show when an Over The Air update fails,
 * have a look at the readme for more details - /connect/README.md
 */
class OtaFailedFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentOtaFailedBinding::bind)
    private val timeHelper: FingerprintTimeHelper by inject()
    private val sessionManager: FingerprintSessionEventsManager by inject()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_ota_failed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(AlertScreenEventWithScannerIssue(timeHelper.now(), ConnectScannerIssue.OtaFailed))

        connectScannerViewModel.setBackButtonToExitWithError()
        binding.continueButton.setOnClickListener {
            connectScannerViewModel.finishAfterError.postEvent()
        }
    }

    private fun setTextInLayout() {
        binding.apply {
            otaFailedTitleTextView.text = getString(R.string.ota_failed_title)
            otaFailedInstructionsTextView.text = getString(R.string.ota_failed_instructions)
            continueButton.text = getString(R.string.continue_button)
        }
    }
}
