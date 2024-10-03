package com.simprints.infra.license.models

import kotlin.text.isNotBlank

@JvmInline
value class LicenseVersion(val value: String) {

    val isLimited: Boolean
        get() = value.isNotBlank()

    companion object {
        val UNLIMITED = LicenseVersion("")
    }
}
