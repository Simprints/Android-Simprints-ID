package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.moduleapi.fingerprint.requests.IFingerprintConfigurationRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class FingerprintConfigurationRequest(
    override val type: FingerprintRequestType = FingerprintRequestType.CONFIGURATION
) : FingerprintRequest

fun FingerprintConfigurationRequest.fromDomainToModuleApi(): IFingerprintConfigurationRequest =
    IFingerprintConfigurationRequestImpl()

@Parcelize
private class IFingerprintConfigurationRequestImpl : IFingerprintConfigurationRequest
