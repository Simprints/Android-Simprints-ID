package com.simprints.core.domain.reference

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class BiometricReference(
    val referenceId: String,
    val modality: Modality,
    val format: String,
    val templates: List<BiometricTemplate>,
) : Parcelable
