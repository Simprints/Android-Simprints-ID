package com.simprints.infra.protection.auxiliary

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class TemplateAuxData(
    val subjectId: String,
    val exponents: IntArray,
    val coefficients: IntArray,
) : Parcelable
