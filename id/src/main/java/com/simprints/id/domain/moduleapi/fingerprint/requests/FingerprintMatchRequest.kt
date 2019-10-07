package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.fromDomainToModuleApi
import com.simprints.moduleapi.face.requests.IFaceRequestType
import com.simprints.moduleapi.fingerprint.requests.IFingerprintMatchRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintSample
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FingerprintMatchRequest(
    val probeFingerprintSamples: List<FingerprintSample>,
    val queryForCandidates: Serializable
) : FingerprintRequest


fun FingerprintMatchRequest.fromDomainToModuleApi(): IFingerprintMatchRequest =
    FingerprintMatchRequestImpl(
        probeFingerprintSamples.map { it.fromDomainToModuleApi() },
        queryForCandidates
    )

@Parcelize
private data class FingerprintMatchRequestImpl(
    override val probeFingerprintSamples: List<IFingerprintSample>,
    override val queryForCandidates: Serializable,
    val type: IFaceRequestType = IFaceRequestType.MATCH
) : IFingerprintMatchRequest


