package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.domain.reference.BiometricReference
import kotlinx.parcelize.Parcelize

@Parcelize
class Identity(
    val subjectId: String,
    val references: List<BiometricReference>,
) : Parcelable
