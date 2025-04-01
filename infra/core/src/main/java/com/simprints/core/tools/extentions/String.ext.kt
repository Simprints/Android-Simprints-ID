package com.simprints.core.tools.extentions

fun String.nullIfEmpty() = takeIf { this.isNotEmpty() }
