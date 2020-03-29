package com.simprints.fingerprint.tools.nfc

interface ComponentNfcAdapter {

    /**
     *  True if the device does not have NFC capability
     */
    fun isNull(): Boolean

    fun isEnabled(): Boolean

}
