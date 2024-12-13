package com.simprints.fingerprint.connect

import android.os.Bundle
import com.simprints.fingerprint.connect.screens.controller.ConnectScannerControllerFragmentArgs
import com.simprints.infra.config.store.models.FingerprintConfiguration

object FingerprintConnectContract {
    val DESTINATION = R.id.connectScannerControllerFragment

    fun getArgs(fingerprintSDK: FingerprintConfiguration.BioSdk): Bundle = ConnectScannerControllerFragmentArgs(
        FingerprintConnectParams(fingerprintSDK),
    ).toBundle()
}
