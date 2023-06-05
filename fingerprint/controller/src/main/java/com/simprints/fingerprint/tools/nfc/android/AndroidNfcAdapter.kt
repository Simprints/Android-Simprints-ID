package com.simprints.fingerprint.tools.nfc.android

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import com.simprints.fingerprint.tools.nfc.ComponentMifareUltralight
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.ComponentNfcTag

class AndroidNfcAdapter(private val nfcAdapter: NfcAdapter?) : ComponentNfcAdapter {

    override fun isNull(): Boolean = nfcAdapter == null

    override fun isEnabled(): Boolean = nfcAdapter!!.isEnabled

    override fun enableReaderMode(activity: Activity, callback: (tag: ComponentNfcTag?) -> Unit, flags: Int, extras: Bundle?) {
        nfcAdapter!!.enableReaderMode(activity, { tag: Tag? -> callback(AndroidNfcTag(tag)) }, flags, extras)
    }

    override fun disableReaderMode(activity: Activity) {
        nfcAdapter!!.disableReaderMode(activity)
    }

    override fun getMifareUltralight(tag: ComponentNfcTag?): ComponentMifareUltralight? =
        tag
            ?.let { MifareUltralight.get((tag as AndroidNfcTag).tag) }
            ?.let { AndroidMifareUltralight(it) }
}
