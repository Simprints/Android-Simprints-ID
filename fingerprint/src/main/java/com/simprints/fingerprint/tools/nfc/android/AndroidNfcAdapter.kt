package com.simprints.fingerprint.tools.nfc.android

import android.nfc.NfcAdapter
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter

class AndroidNfcAdapter(val nfcAdapter: NfcAdapter?) : ComponentNfcAdapter {

    override fun isNull(): Boolean = nfcAdapter == null

    override fun isEnabled(): Boolean = nfcAdapter!!.isEnabled
}
