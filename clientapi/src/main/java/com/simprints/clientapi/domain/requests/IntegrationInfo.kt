package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IIntegrationInfo
import com.simprints.moduleapi.app.requests.IOdkIntegrationInfo
import com.simprints.moduleapi.app.requests.IStandardIntegrationInfo
import kotlinx.android.parcel.Parcelize


enum class IntegrationInfo {
    ODK, STANDARD;

    fun toAppIntegrationInfo(): IIntegrationInfo =
        when (this) {
            ODK -> OdkIntegrationInfo()
            STANDARD -> StandardIntegrationInfo()
        }

    @Parcelize
    private class OdkIntegrationInfo : IOdkIntegrationInfo

    @Parcelize
    private class StandardIntegrationInfo : IStandardIntegrationInfo
}


