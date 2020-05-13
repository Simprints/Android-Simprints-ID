package com.simprints.core.tools

import android.util.Base64

object EncodingUtils {
    fun byteArrayToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    @Throws(IllegalArgumentException::class)
    fun base64ToBytes(base64: String): ByteArray {
        return Base64.decode(base64, Base64.DEFAULT)
    }
}
