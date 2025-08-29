package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.domain.modality.Modality
import kotlinx.parcelize.Parcelize

/**
 * Identity is a set of biometric templates of the same known subject.
 */
@Parcelize
class Identity(
    val subjectId: String,
    val modality: Modality,
    val samples: List<Sample>,
) : Parcelable
