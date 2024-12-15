package com.simprints.core.tools.utils

import android.util.Base64

interface EncodingUtils {
    fun byteArrayToBase64(bytes: ByteArray): String

    fun base64ToBytes(base64: String): ByteArray
}

object EncodingUtilsImpl : EncodingUtils {
    // Do not use in unit test - android.util.Base64 requires android sdk
    override fun byteArrayToBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    @Throws(IllegalArgumentException::class)
    override fun base64ToBytes(base64: String): ByteArray = Base64.decode(base64, Base64.NO_WRAP)
}
