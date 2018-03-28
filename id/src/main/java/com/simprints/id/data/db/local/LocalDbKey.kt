package com.simprints.id.data.db.local

import com.simprints.id.data.db.remote.models.fs_RealmKeys
import java.util.*


data class LocalDbKey(val projectId: String, val value: ByteArray, val legacyApiKey: String = "") {

    constructor(realmKeys: fs_RealmKeys) : this(
        realmKeys.projectId,
        realmKeys.value.toBytes(),
        realmKeys.legacyValue
    )

    val legacyRealmKey: ByteArray = Arrays.copyOf(legacyApiKey.toByteArray(), 64)

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
