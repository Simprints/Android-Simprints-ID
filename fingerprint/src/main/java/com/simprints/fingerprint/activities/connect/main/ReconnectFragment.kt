package com.simprints.fingerprint.activities.connect.main

import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.databinding.FragmentReconnectBinding

class ReconnectFragment : ConnectFragment(R.layout.fragment_reconnect) {
    private val binding by viewBinding(FragmentReconnectBinding::bind)

    override fun initUiComponents() {
        binding.reconnectMessageTextView.text = getString(R.string.reconnecting_message)
    }

    override fun observeScannerEvents() {
        connectScannerViewModel.showScannerErrorDialogWithScannerId.fragmentObserveEventWith { showDialogForScannerErrorConfirmation(it) }
    }
}
