package com.simprints.fingerprint.activities.connect.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConnectScannerMainFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_connect_scanner_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetFragment = when (connectScannerViewModel.connectMode) {
            ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT -> InitialConnectFragment()
            ConnectScannerTaskRequest.ConnectMode.RECONNECT -> ReconnectFragment()
        }

        childFragmentManager.beginTransaction().apply {
            replace(R.id.connect_scanner_main_fragment_container, targetFragment)
            commitNow()
        }
    }
}
