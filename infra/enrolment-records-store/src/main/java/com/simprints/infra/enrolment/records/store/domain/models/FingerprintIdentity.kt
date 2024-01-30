package com.simprints.infra.enrolment.records.store.domain.models

import android.os.Parcelable
import com.simprints.core.domain.fingerprint.FingerprintSample
import kotlinx.parcelize.Parcelize

@Parcelize
//TODO(milen): Change patientId to subjectId
class FingerprintIdentity(val patientId: String,
                          val fingerprints: List<FingerprintSample>): Parcelable
