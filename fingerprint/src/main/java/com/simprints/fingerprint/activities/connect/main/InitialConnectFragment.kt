package com.simprints.fingerprint.activities.connect.main

import com.simprints.fingerprint.R
import kotlinx.android.synthetic.main.fragment_initial_connect.*

class InitialConnectFragment : ConnectFragment(R.layout.fragment_initial_connect) {

    override fun initUiComponents() {}

    override fun observeScannerEvents() {
        connectScannerViewModel.progress.fragmentObserveWith { connectScannerProgressBar.progress = it }
        connectScannerViewModel.message.fragmentObserveWith { connectScannerInfoTextView.text = androidResourcesHelper.getString(it) }
        connectScannerViewModel.showScannerErrorDialogWithScannerId.fragmentObserveEventWith { showDialogForScannerErrorConfirmation(it) }
    }
}
