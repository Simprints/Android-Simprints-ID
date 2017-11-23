package com.simprints.id.tools.extensions

import java.util.regex.Pattern

private val guidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f‌​]{4}-[0-9a-f]{12}$")

fun String.isGuid() = guidPattern.matcher(this).matches()