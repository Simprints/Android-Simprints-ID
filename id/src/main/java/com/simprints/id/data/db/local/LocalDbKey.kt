package com.simprints.id.data.db.local

import java.util.*

data class LocalDbKey(val value: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalDbKey

        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(value)
    }
}
