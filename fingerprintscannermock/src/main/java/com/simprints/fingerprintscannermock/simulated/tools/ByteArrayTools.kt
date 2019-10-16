package com.simprints.fingerprintscannermock.simulated.tools

import com.simprints.fingerprintscanner.v1.Message as MessageV1
import java.io.PipedInputStream
import java.io.PipedOutputStream

fun concatenateByteArrays(byteArrays: Iterable<ByteArray>): ByteArray {
    var totalLength = 0
    byteArrays.forEach { totalLength += it.size }

    val resultArray = ByteArray(totalLength)
    var idx = 0
    for (byteArray in byteArrays)
        for (element in byteArray) {
            resultArray[idx] = element
            idx++
        }
    return resultArray
}

fun bytesToMessageV1(bytes: ByteArray): MessageV1 {
    val tempInputStream = PipedInputStream()
    val tempOutputStream = PipedOutputStream()
        .also {
            it.connect(tempInputStream)
            it.write(bytes)
            it.flush()
        }
    val message = MessageV1.blockingReceiveFrom(tempInputStream, false)
    tempInputStream.close()
    tempOutputStream.close()
    return message
}

/** @throws IndexOutOfBoundsException if odd number of hex characters */
fun hexStringToByteArray(string: String): ByteArray {
    val s = stripWhiteSpaceAndMakeLowercase(string)
    val len = s.length
    val data = ByteArray(len / 2)
    var i = 0

    while (i < len) {
        data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }

    return data
}

private fun stripWhiteSpaceAndMakeLowercase(string: String) = string
    .replace(" ", "")
    .replace("\n", "")
    .replace("\r", "")
    .toLowerCase()

fun byteArrayFromHexString(vararg strings: String): ByteArray {
    val result = mutableListOf<ByteArray>()
    for (s in strings) result.add(hexStringToByteArray(s))
    return concatenateByteArrays(result)
}
