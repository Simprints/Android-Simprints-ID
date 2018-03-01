package com.simprints.id.tools.extensions

import java.math.BigInteger
import java.security.MessageDigest
import java.util.regex.Pattern

private val guidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f‌​]{4}-[0-9a-f]{12}$")

fun String.isGuid() = guidPattern.matcher(this).matches()

fun String.md5(): String {
    val m = MessageDigest.getInstance("MD5")
    m.update(this.toByteArray(), 0, this.length)
    val i = BigInteger(1, m.digest())
    return String.format("%1$032x", i)
}
