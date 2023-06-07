package com.simprints.fingerprint.tools.nfc.android

import android.nfc.tech.MifareUltralight
import com.simprints.fingerprint.tools.nfc.ComponentMifareUltralight
import java.io.Closeable

class AndroidMifareUltralight(
    val mifare: MifareUltralight
) : ComponentMifareUltralight, Closeable by mifare {

    override fun connect() {
        mifare.connect()
    }

    override fun readPages(pageOffset: Int): ByteArray =
        mifare.readPages(pageOffset)
}
