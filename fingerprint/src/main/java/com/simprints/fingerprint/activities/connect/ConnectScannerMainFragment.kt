package com.simprints.fingerprint.activities.connect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForConnectScannerActivityException
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConnectScannerMainFragment : FingerprintFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_connect_scanner_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectScannerRequest: ConnectScannerTaskRequest =
            arguments?.getParcelable(ConnectScannerTaskRequest.BUNDLE_KEY) as ConnectScannerTaskRequest?
                ?: throw InvalidRequestForConnectScannerActivityException()

        val targetFragment = when (connectScannerRequest.connectMode) {
            ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT -> InitialConnectFragment()
            ConnectScannerTaskRequest.ConnectMode.RECONNECT -> ReconnectFragment()
        }

        childFragmentManager.beginTransaction().apply {
            replace(R.id.connect_scanner_main_fragment_container, targetFragment)
            commitNow()
        }
    }
}
