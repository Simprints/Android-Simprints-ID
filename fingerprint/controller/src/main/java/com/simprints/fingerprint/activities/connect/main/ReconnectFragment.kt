package com.simprints.fingerprint.activities.connect.main

import com.simprints.fingerprint.R
import com.simprints.fingerprint.databinding.FragmentReconnectBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class ReconnectFragment : ConnectFragment(R.layout.fragment_reconnect) {
    private val binding by viewBinding(FragmentReconnectBinding::bind)

    override fun initUiComponents() {
        binding.reconnectMessageTextView.text = getString(IDR.string.reconnecting_message)
    }

    override fun observeScannerEvents() {
        connectScannerViewModel.showScannerErrorDialogWithScannerId.fragmentObserveEventWith { showDialogForScannerErrorConfirmation(it) }
    }
}
