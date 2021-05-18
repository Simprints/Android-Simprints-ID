package com.simprints.eventsystem.subject.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintIdentity(val patientId: String,
                          val fingerprints: List<FingerprintSample>): Parcelable
