package com.simprints.fingerprint.activities.connect.issues.otafailed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.tools.livedata.postEvent
import kotlinx.android.synthetic.main.fragment_ota_failed.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OtaFailedFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val resourceHelper: FingerprintAndroidResourcesHelper by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_ota_failed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        continueButton.setOnClickListener {
            connectScannerViewModel.finishAfterError.postEvent()
        }
    }

    private fun setTextInLayout() {
        with(resourceHelper) {
            otaFailedTitleTextView.text = getString(R.string.ota_failed_title)
            otaFailedInstructionsTextView.text = getString(R.string.ota_failed_instructions)
            continueButton.text = getString(R.string.continue_button)
        }
    }
}
