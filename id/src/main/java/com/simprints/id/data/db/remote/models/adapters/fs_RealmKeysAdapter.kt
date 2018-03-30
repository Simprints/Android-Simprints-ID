package com.simprints.id.data.db.remote.models.adapters

import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.remote.models.fs_RealmKeys

fun fs_RealmKeys.toLocalDbKey(): LocalDbKey {
    return LocalDbKey(
        projectId = this.projectId,
        value = this.value.toBytes(),
        legacyApiKey = this.legacyValue
    )
}
