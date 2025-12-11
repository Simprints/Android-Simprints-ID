package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricTemplate
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class Sample(
    val id: String = UUID.randomUUID().toString(),
    val modality: Modality,
    val referenceId: String,
    val format: String,
    val template: BiometricTemplate,
) : Parcelable {
    // TODO: due to random IDs, direct comparison of data classes is broken
    //   this will be fixed in future refactoring of sample class
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sample

        return template == other.template
    }

    override fun hashCode(): Int = template.hashCode()
}
