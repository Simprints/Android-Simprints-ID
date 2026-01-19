package com.simprints.fingerprint.connect

import com.simprints.infra.config.store.models.ModalitySdkType

object FingerprintConnectContract {
    val DESTINATION = R.id.connectScannerControllerFragment

    fun getParams(fingerprintSDK: ModalitySdkType) = FingerprintConnectParams(fingerprintSDK)
}
