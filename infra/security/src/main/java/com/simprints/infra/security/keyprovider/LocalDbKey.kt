package com.simprints.infra.security.keyprovider

import androidx.annotation.Keep

@Keep
data class LocalDbKey(
    val projectId: String,
    val value: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalDbKey) return false
        return value.contentEquals(other.value) && projectId == other.projectId
    }

    override fun hashCode() = value.contentHashCode() + projectId.hashCode()
}
