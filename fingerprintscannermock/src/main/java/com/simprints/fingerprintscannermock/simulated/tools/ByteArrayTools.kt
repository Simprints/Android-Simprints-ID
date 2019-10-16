package com.simprints.fingerprintscannermock.simulated.tools

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
    return result.reduce { acc, bytes -> acc + bytes }
}
