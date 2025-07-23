package com.simprints.fingerprint.connect

import com.simprints.infra.config.store.models.FingerprintConfiguration

object FingerprintConnectContract {
    val DESTINATION = R.id.connectScannerControllerFragment

    fun getParams(fingerprintSDK: FingerprintConfiguration.BioSdk) = FingerprintConnectParams(fingerprintSDK)
}
