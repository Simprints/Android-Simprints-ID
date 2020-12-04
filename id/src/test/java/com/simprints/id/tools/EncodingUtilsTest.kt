package com.simprints.id.tools

import com.simprints.id.tools.utils.EncodingUtils
import java.util.*

// Encoder that doesn't depend on  android.util.Base64
// so usable in unit tests
class EncodingUtilsTest: EncodingUtils {
    override fun byteArrayToBase64(bytes: ByteArray): String =
        Base64.getEncoder().encodeToString(bytes)

    override fun base64ToBytes(base64: String): ByteArray =
        Base64.getDecoder().decode(base64)
}
