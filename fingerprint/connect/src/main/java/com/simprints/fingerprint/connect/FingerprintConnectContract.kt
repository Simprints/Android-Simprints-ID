package com.simprints.fingerprint.connect

import android.os.Bundle
import com.simprints.fingerprint.connect.screens.controller.ConnectScannerControllerFragmentArgs

object FingerprintConnectContract {

    val DESTINATION = R.id.connectScannerControllerFragment

    const val RESULT = "connect_contract"

    fun getArgs(isReconnect: Boolean): Bundle= ConnectScannerControllerFragmentArgs(
        FingerprintConnectParams(isReconnect)
    ).toBundle()

}
