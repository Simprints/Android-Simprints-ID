package com.simprints.fingerprint.activities.connect.issues.otarecovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class OtaRecoveryFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val resourceHelper: FingerprintAndroidResourcesHelper by inject()

    private val viewModel: OtaRecoveryViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_scanner_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()


    }

    private fun setTextInLayout() {
        with(resourceHelper) {

        }
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
