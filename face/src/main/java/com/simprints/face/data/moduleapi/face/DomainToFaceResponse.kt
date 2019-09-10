package com.simprints.face.data.moduleapi.face

import android.os.Parcelable
import com.simprints.face.data.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.face.data.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchingResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceTier
import com.simprints.face.data.moduleapi.face.responses.entities.FaceTier.*
import com.simprints.face.exceptions.InvalidFaceResponseException
import com.simprints.moduleapi.face.responses.*
import com.simprints.moduleapi.face.responses.entities.IFaceMatchingResult
import com.simprints.moduleapi.face.responses.entities.IFaceTier
import kotlinx.android.parcel.Parcelize

object DomainToFaceResponse {

    fun fromDomainToFaceResponse(fingerprintResponse: FaceResponse): IFaceResponse =
        when (fingerprintResponse) {
            is FaceEnrolResponse ->
                fromDomainToFaceEnrolResponse(fingerprintResponse)
            is FaceVerifyResponse ->
                fromDomainToFaceVerifyResponse(fingerprintResponse)
            is FaceIdentifyResponse ->
                fromDomainToFaceIdentifyResponse(fingerprintResponse)
            else -> throw InvalidFaceResponseException()
        }

    private fun fromDomainToFaceEnrolResponse(enrol: FaceEnrolResponse): IFaceEnrolResponse = IFaceEnrolResponseImpl(enrol.guid)

    private fun fromDomainToFaceVerifyResponse(verify: FaceVerifyResponse): IFaceVerifyResponse {
        val matchingResult = IFaceMatchingResultImpl(
            verify.matchingResult.guidFound,
            verify.matchingResult.confidence,
            fromDomainToIFaceResponseTier(verify.matchingResult.tier))

        return IFaceVerifyResponseImpl(matchingResult)
    }

    private fun fromDomainToFaceIdentifyResponse(identify: FaceIdentifyResponse): IFaceIdentifyResponse =
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

// TODO: temporary implementation
@Parcelize
private class IFaceEnrolResponseImpl(
    override val guid: String,
    override val type: IFaceResponseType = IFaceResponseType.CAPTURE
) : IFaceEnrolResponse

// TODO: temporary implementation
@Parcelize
private class IFaceIdentifyResponseImpl(
    override val identifications: List<IFaceMatchingResult>,
    override val type: IFaceResponseType = IFaceResponseType.IDENTIFY
) : IFaceIdentifyResponse

// TODO: temporary implementation
@Parcelize
private class IFaceVerifyResponseImpl(
    override val matchingResult: IFaceMatchingResult,
    override val type: IFaceResponseType = IFaceResponseType.VERIFY
) : IFaceVerifyResponse

@Parcelize
private data class IFaceMatchingResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IFaceTier) : Parcelable, IFaceMatchingResult
