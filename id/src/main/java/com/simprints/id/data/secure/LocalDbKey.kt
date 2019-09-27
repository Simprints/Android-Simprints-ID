package com.simprints.id.data.secure

import androidx.annotation.Keep
import java.util.*

@Keep
data class LocalDbKey(val projectId: String, val value: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalDbKey

        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode() = Arrays.hashCode(value)
}
