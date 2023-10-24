package com.simprints.matcher

import android.os.Parcelable
import com.simprints.core.domain.common.FlowProvider
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import kotlinx.parcelize.Parcelize

@Parcelize
data class MatchParams(
    val probeFaceSamples: List<FaceSample> = emptyList(),
    val probeFingerprintSamples: List<FingerprintSample> = emptyList(),
    val flowType: FlowProvider.FlowType,
    val queryForCandidates: SubjectQuery,
) : Parcelable {

    fun isFaceMatch() = probeFaceSamples.isNotEmpty()

    @ExcludedFromGeneratedTestCoverageReports("Generated code")
    @Parcelize
    data class FaceSample(
        val faceId: String,
        val template: ByteArray,
    ) : Parcelable {

        // Auto-generated by Android Studio to ensure
        // byte array field is compared correctly
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FaceSample

            if (faceId != other.faceId) return false
            if (!template.contentEquals(other.template)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = faceId.hashCode()
            result = 31 * result + template.contentHashCode()
            return result
        }
    }

    @ExcludedFromGeneratedTestCoverageReports("Generated code")
    @Parcelize
    data class FingerprintSample(
        val fingerId: IFingerIdentifier,
        val format: String,
        val template: ByteArray,
    ) : Parcelable {

        // Auto-generated by Android Studio to ensure
        // byte array field is compared correctly
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FingerprintSample

            if (fingerId != other.fingerId) return false
            if (format != other.format) return false
            if (!template.contentEquals(other.template)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fingerId.hashCode()
            result = 31 * result + format.hashCode()
            result = 31 * result + template.contentHashCode()
            return result
        }
    }
}