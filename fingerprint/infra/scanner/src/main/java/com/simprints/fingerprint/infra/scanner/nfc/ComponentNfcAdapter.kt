package com.simprints.fingerprint.infra.scanner.nfc

import android.app.Activity
import android.os.Bundle

interface ComponentNfcAdapter {
    /**
     *  True if the device does not have NFC capability
     */
    fun isNull(): Boolean

    fun isEnabled(): Boolean

    fun enableReaderMode(
        activity: Activity,
        callback: (tag: ComponentNfcTag?) -> Unit,
        flags: Int,
        extras: Bundle?,
    )

    fun disableReaderMode(activity: Activity)

    fun getMifareUltralight(tag: ComponentNfcTag?): ComponentMifareUltralight?

    companion object {
        const val FLAG_READER_NFC_A = 1
        const val FLAG_READER_SKIP_NDEF_CHECK = 128
    }
}
