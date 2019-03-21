package com.simprints.id.domain.moduleapi.face

import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchingResult
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceTier
import com.simprints.moduleapi.face.responses.*

object FaceToDomainResponse {

    fun fromFaceToDomainResponse(faceResponse: IFaceResponse): FaceResponse =
        when (faceResponse) {
            is IFaceEnrolResponse -> fromFaceToDomainEnrolResponse(faceResponse)
            is IFaceVerifyResponse -> fromFaceToDomainVerifyResponse(faceResponse)
            is IFaceIdentifyResponse -> fromFaceToDomainIdentifyResponse(faceResponse)
            else -> throw IllegalStateException("Invalid face request")
        }

    private fun fromFaceToDomainVerifyResponse(faceResponse: IFaceVerifyResponse): FaceVerifyResponse {
        with(faceResponse.matchingResult) {
            val matchResult = FaceMatchingResult(guid, confidence, fromFaceToDomainTier(tier))
            return FaceVerifyResponse(matchResult)
        }
    }

    private fun fromFaceToDomainEnrolResponse(faceResponse: IFaceEnrolResponse): FaceEnrolResponse =
        FaceEnrolResponse(faceResponse.guid)

    private fun fromFaceToDomainIdentifyResponse(faceResponse: IFaceIdentifyResponse): FaceIdentifyResponse =
        FaceIdentifyResponse(faceResponse.identifications.map { fromFaceToDomainIdentificationResult(it) })

    private fun fromFaceToDomainIdentificationResult(identification: IFaceMatchingResult): FaceMatchingResult =
        FaceMatchingResult(identification.guid, identification.confidence, fromFaceToDomainTier(identification.tier))

    private fun fromFaceToDomainTier(tier: IFaceTier): FaceTier =
        when (tier) {
            IFaceTier.TIER_1 -> FaceTier.TIER_1
            IFaceTier.TIER_2 -> FaceTier.TIER_2
            IFaceTier.TIER_3 -> FaceTier.TIER_3
            IFaceTier.TIER_4 -> FaceTier.TIER_4
            IFaceTier.TIER_5 -> FaceTier.TIER_5
        }
}
