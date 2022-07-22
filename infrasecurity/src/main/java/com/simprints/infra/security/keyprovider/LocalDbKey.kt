package com.simprints.infra.security.keyprovider

import androidx.annotation.Keep

@Keep
data class LocalDbKey(val projectId: String, val value: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalDbKey

        if (!value.contentEquals(other.value) || projectId != other.projectId) return false

        return true
    }

    override fun hashCode() = value.contentHashCode() + projectId.hashCode()
}
