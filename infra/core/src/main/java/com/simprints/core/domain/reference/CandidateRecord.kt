package com.simprints.core.domain.reference

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CandidateRecord(
    val subjectId: String,
    val references: List<BiometricReference>,
) : Parcelable
