package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintIdentity(val patientId: String,
                          val fingerprints: List<FingerprintSample>): Parcelable
