package com.simprints.fingerprint.tools.nfc

import java.io.Closeable
import java.io.IOException

interface ComponentMifareUltralight : Closeable {

    @Throws(IOException::class)
    fun connect()

    @Throws(IOException::class)
    fun readPages(pageOffset: Int): ByteArray
}
