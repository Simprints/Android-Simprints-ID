package com.simprints.id.data.db.remote.people.models

import com.simprints.id.data.db.remote.models.fb_Fingerprint

data class RemoteFingerprint(
    val quality: Int,
    val template: String
)

fun fb_Fingerprint.toRemoteFingerprint(): RemoteFingerprint =
    RemoteFingerprint(
        quality = quality,
        template = template
    )
