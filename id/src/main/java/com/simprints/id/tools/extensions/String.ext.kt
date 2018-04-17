package com.simprints.id.tools.extensions

import java.util.regex.Pattern

private val guidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f‌​]{4}-[0-9a-f]{12}$")

fun String.isGuid() = guidPattern.matcher(this).matches()
fun String.fromLowerCamelToLowerUnderscore(): String {
    val lowerCamelCase = "someLowerCaseCamel"
    val m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(lowerCamelCase)

    val sb = StringBuffer()
    while (m.find()) {
        m.appendReplacement(sb, "_" + m.group().toLowerCase())
    }
    m.appendTail(sb)

    return sb.toString()
}
