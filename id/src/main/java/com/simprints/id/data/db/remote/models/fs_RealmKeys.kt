package com.simprints.id.data.db.remote.models

import com.google.firebase.firestore.Blob

data class fs_RealmKeys(val projectId: String = "",
                        var value: Blob = Blob.fromBytes(byteArrayOf()),
                        val legacyValue: String = "")
