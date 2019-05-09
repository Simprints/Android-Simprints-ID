package com.simprints.id.tools.extensions

fun ByteArray.toHex(): String {

    val hexValues = "0123456789ABCDEF".toCharArray()
    val hexChars = CharArray(this.size * 2)
    for (j in this.indices) {
        val v = this[j].toInt() and 0xFF // Here is the conversion
        hexChars[j * 2] = hexValues[v.ushr(4)]
        hexChars[j * 2 + 1] = hexValues[v and 0x0F]
    }

    return String(hexChars)
}
