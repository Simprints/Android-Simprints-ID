package com.simprints.fingerprint.activities.connect.main

import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.databinding.FragmentInitialConnectBinding

class InitialConnectFragment : ConnectFragment(R.layout.fragment_initial_connect) {
    val binding by viewBinding(FragmentInitialConnectBinding::bind)

    override fun initUiComponents() {}

    override fun observeScannerEvents() {
        connectScannerViewModel.progress.fragmentObserveWith { binding.connectScannerProgressBar.progress = it }
        connectScannerViewModel.message.fragmentObserveWith { binding.connectScannerInfoTextView.text = getString(it) }
        connectScannerViewModel.showScannerErrorDialogWithScannerId.fragmentObserveEventWith { showDialogForScannerErrorConfirmation(it) }
    }
}
