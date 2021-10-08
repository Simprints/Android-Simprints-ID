package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.core.domain.fingerprint.FingerprintSample
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintIdentity(val patientId: String,
                          val fingerprints: List<FingerprintSample>): Parcelable
