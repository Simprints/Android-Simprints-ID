package com.simprints.id.commontesttools

import com.google.common.io.BaseEncoding
import com.simprints.id.tools.utils.EncodingUtils

// EncodingUtilsImpl uses android.util.Base64 that is available only with android sdk
// and that requires Robolectric
val encodingUtilsForTests = EncodingUtilsImplForTests()

class EncodingUtilsImplForTests: EncodingUtils {

    private val encoder = BaseEncoding.base64()

    override fun byteArrayToBase64(bytes: ByteArray): String =
        encoder.encode(bytes)


    override fun base64ToBytes(base64: String): ByteArray =
        encoder.decode(base64)
}
