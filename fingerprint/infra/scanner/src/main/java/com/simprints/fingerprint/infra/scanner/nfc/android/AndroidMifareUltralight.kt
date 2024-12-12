package com.simprints.fingerprint.infra.scanner.nfc.android

import android.nfc.tech.MifareUltralight
import com.simprints.fingerprint.infra.scanner.nfc.ComponentMifareUltralight
import java.io.Closeable

internal class AndroidMifareUltralight(
    private val mifare: MifareUltralight,
) : ComponentMifareUltralight,
    Closeable by mifare {
    override fun connect() {
        mifare.connect()
    }

    override fun readPages(pageOffset: Int): ByteArray = mifare.readPages(pageOffset)
}
