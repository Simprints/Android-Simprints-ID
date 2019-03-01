package com.simprints.id.domain.matching

import android.os.Parcelable
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse.IIdentificationResult
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponseTier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IdentificationResult(val guidFound: String,
                                val confidence: Int,
                                val tier: Tier): Parcelable

fun IdentificationResult.toClientApiIdentificationResult():IIdentificationResult =
    ClientApiIdentificationResult(guidFound, confidence, tier.toClientApiIClientApiResponseTier())

@Parcelize
private data class ClientApiIdentificationResult(
    override val guid: String,
    override val confidence: Int,
    override val tier: IClientApiResponseTier): Parcelable, IIdentificationResult
