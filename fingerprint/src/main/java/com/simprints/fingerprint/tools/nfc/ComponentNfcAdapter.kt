package com.simprints.fingerprint.tools.nfc

interface ComponentNfcAdapter {

    /**
     *  True if the device does not have NFC capability
     */
    fun isNull(): Boolean

    fun isEnabled(): Boolean

    companion object {
        const val ACTION_ADAPTER_STATE_CHANGED = "android.nfc.action.ADAPTER_STATE_CHANGED"
        const val EXTRA_ADAPTER_STATE = "android.nfc.extra.ADAPTER_STATE"
        const val STATE_OFF = 1
        const val STATE_ON = 3
    }
}
