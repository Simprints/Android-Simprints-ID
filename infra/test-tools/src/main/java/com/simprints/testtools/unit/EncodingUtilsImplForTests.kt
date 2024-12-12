package com.simprints.testtools.unit

import android.annotation.SuppressLint
import com.simprints.core.tools.utils.EncodingUtils
import java.util.Base64

// EncodingUtilsImpl uses android.util.Base64 that is available only with android sdk
// and that requires Robolectric
@SuppressLint("NewApi")
object EncodingUtilsImplForTests : EncodingUtils {
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    override fun byteArrayToBase64(bytes: ByteArray): String = encoder.encodeToString(bytes)

    override fun base64ToBytes(base64: String): ByteArray = decoder.decode(base64)
}
