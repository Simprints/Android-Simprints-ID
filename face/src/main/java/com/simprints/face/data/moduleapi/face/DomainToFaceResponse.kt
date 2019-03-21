package com.simprints.face.data.moduleapi.face

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchingResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceTier
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceTier.*
import com.simprints.moduleapi.face.responses.*
import kotlinx.android.parcel.Parcelize

object DomainToFaceResponse {

    fun fromDomainToFaceEnrolResponse(enrol: FaceEnrolResponse): IFaceEnrolResponse = IFaceEnrolResponseImpl(enrol.guid)

    fun fromDomainToFaceVerifyResponse(verify: FaceVerifyResponse): IFaceVerifyResponse {
        val matchingResult = IFaceMatchingResultImpl(
            verify.matchingResult.guidFound,
            verify.matchingResult.confidence,
            fromDomainToIFaceResponseTier(verify.matchingResult.tier))

        return IFaceVerifyResponseImpl(matchingResult)
    }

    fun fromDomainToFaceIdentifyResponse(identify: FaceIdentifyResponse): IFaceIdentifyResponse =
        IFaceIdentifyResponseImpl(identify.identifications.map { fromDomainToFaceIdentificationResult(it) })

    private fun fromDomainToFaceIdentificationResult(result: FaceMatchingResult): IFaceMatchingResult =
        IFaceMatchingResultImpl(result.guidFound, result.confidence, fromDomainToIFaceResponseTier(result.tier))

    private fun fromDomainToIFaceResponseTier(tier: FaceTier): IFaceTier =
        when (tier) {
            TIER_1 -> IFaceTier.TIER_1
            TIER_2 -> IFaceTier.TIER_2
            TIER_3 -> IFaceTier.TIER_3
            TIER_4 -> IFaceTier.TIER_4
            TIER_5 -> IFaceTier.TIER_5
        }
}

@Parcelize
private class IFaceEnrolResponseImpl(override val guid: String) : IFaceEnrolResponse

@Parcelize
private class IFaceIdentifyResponseImpl(
    override val identifications: List<IFaceMatchingResult>) : IFaceIdentifyResponse


@Parcelize
private class IFaceVerifyResponseImpl(override val matchingResult: IFaceMatchingResult) : IFaceVerifyResponse

@Parcelize
private data class IFaceMatchingResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IFaceTier) : Parcelable, IFaceMatchingResult
