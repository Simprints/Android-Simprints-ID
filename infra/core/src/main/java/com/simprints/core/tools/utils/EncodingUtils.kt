package com.simprints.core.tools.utils

import android.util.Base64
import java.nio.ByteBuffer

interface EncodingUtils {
    fun byteArrayToBase64(bytes: ByteArray): String

    fun base64ToBytes(base64: String): ByteArray

    fun floatArrayToBase64(floatArray: FloatArray): String

    fun base64ToFloatArray(base64String: String): FloatArray
}

object EncodingUtilsImpl : EncodingUtils {
    // Do not use in unit test - android.util.Base64 requires android sdk
    override fun byteArrayToBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    @Throws(IllegalArgumentException::class)
    override fun base64ToBytes(base64: String): ByteArray = Base64.decode(base64, Base64.NO_WRAP)

    override fun floatArrayToBase64(floatArray: FloatArray): String {
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
        for (value in floatArray) {
            byteBuffer.putFloat(value)
        }
        return Base64.encodeToString(byteBuffer.array(), Base64.NO_WRAP)
    }

    override fun base64ToFloatArray(base64String: String): FloatArray {
        val byteArray = Base64.decode(base64String, Base64.NO_WRAP)
        val byteBuffer = ByteBuffer.wrap(byteArray)
        val floatArray = FloatArray(byteArray.size / 4)
        for (i in floatArray.indices) {
            val value = byteBuffer.getFloat()
            floatArray[i] = if (value.isNaN()) 0.0f else value
        }
        return floatArray
    }
}

fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
    val byteBuffer = ByteBuffer.wrap(byteArray)
    val floatArray = FloatArray(byteArray.size / 4) // Each float is 4 bytes
    for (i in floatArray.indices) {
        val value = byteBuffer.getFloat()
        floatArray[i] = if (value.isNaN()) 0.0f else value
    }
    return floatArray
}

fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
    for (value in floatArray) {
        byteBuffer.putFloat(value)
    }
    return byteBuffer.array()
}
