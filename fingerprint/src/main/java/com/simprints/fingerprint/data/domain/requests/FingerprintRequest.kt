package com.simprints.fingerprint.data.domain.requests

import android.os.Parcelable

interface FingerprintRequest : Parcelable {
    val projectId: String
    val userId: String
    val moduleId: String
    val metadata: String
}
