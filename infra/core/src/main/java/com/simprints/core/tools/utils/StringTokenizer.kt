package com.simprints.core.tools.utils

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.DeterministicAead
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.daead.DeterministicAeadConfig
import javax.inject.Inject

class StringTokenizer @Inject constructor(
    private val encodingUtils: EncodingUtils,
) {
    init {
        DeterministicAeadConfig.register()
    }

    fun encrypt(
        value: String,
        keysetJson: String,
    ): String = buildDeaed(keysetJson)
        .encryptDeterministically(value.toByteArray(), null)
        .run(encodingUtils::byteArrayToBase64)

    fun decrypt(
        value: String,
        keysetJson: String,
    ): String = buildDeaed(keysetJson)
        .decryptDeterministically(encodingUtils.base64ToBytes(value), null)
        .run(::String)

    private fun buildDeaed(keysetJson: String) = CleartextKeysetHandle
        .read(JsonKeysetReader.withString(keysetJson))
        .getPrimitive(DeterministicAead::class.java)
}
