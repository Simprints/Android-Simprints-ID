package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.fromDomainToModuleApi
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.requests.IFingerprintMatchRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponseType
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FingerprintMatchRequest(
    val probeFingerprintSamples: List<FingerprintCaptureSample>,
    val queryForCandidates: Serializable,
    override val type: FingerprintRequestType = FingerprintRequestType.MATCH
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
    val type: IFingerprintResponseType = IFingerprintResponseType.MATCH
) : IFingerprintMatchRequest


