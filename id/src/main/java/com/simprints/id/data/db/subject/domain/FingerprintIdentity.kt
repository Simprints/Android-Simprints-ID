package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintIdentity(val patientId: String,
                          val fingerprints: List<FingerprintSample>): Parcelable
