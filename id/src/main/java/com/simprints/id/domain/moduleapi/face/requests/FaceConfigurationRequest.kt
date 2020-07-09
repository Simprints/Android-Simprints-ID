package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceConfigurationRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceConfigurationRequest(
    override val type: FaceRequestType = FaceRequestType.CONFIGURATION
) : FaceRequest

fun FaceConfigurationRequest.fromDomainToModuleApi(): IFaceConfigurationRequest =
    IFaceConfigurationRequestImpl()

@Parcelize
private class IFaceConfigurationRequestImpl : IFaceConfigurationRequest
