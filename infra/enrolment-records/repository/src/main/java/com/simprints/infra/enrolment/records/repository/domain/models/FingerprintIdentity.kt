package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import com.simprints.core.domain.fingerprint.FingerprintSample
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintIdentity(
    val subjectId: String,
    val fingerprints: List<FingerprintSample>,
) : Parcelable
