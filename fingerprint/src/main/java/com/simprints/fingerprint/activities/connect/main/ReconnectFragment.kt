package com.simprints.fingerprint.activities.connect.main

import com.simprints.fingerprint.R
import kotlinx.android.synthetic.main.fragment_reconnect.*

class ReconnectFragment : ConnectFragment(R.layout.fragment_reconnect) {

    override fun initUiComponents() {
        reconnect_message_text_view.text = getString(R.string.reconnecting_message)
    }

    override fun observeScannerEvents() {
        connectScannerViewModel.showScannerErrorDialogWithScannerId.fragmentObserveEventWith { showDialogForScannerErrorConfirmation(it) }
    }
}
