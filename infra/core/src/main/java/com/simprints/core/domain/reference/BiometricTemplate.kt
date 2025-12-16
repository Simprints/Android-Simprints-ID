package com.simprints.core.domain.reference

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class BiometricTemplate(
    val id: String = UUID.randomUUID().toString(),
    val template: ByteArray,
    val identifier: TemplateIdentifier = TemplateIdentifier.NONE,
) : StepParams,
    StepResult,
    Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BiometricTemplate

        // ID is explicitly ignored in the equality check since it is randomly generated on creation and used only locally
        if (!template.contentEquals(other.template)) return false
        if (identifier != other.identifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = template.contentHashCode()
        result = 31 * result + identifier.hashCode()
        return result
    }
}
