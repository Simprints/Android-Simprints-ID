package com.simprints.id.domain.moduleapi.app.requests

import com.simprints.moduleapi.app.requests.IIntegrationInfo
import com.simprints.moduleapi.app.requests.IOdkIntegrationInfo
import com.simprints.moduleapi.app.requests.IStandardIntegrationInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class AppIntegrationInfo : IIntegrationInfo {
    ODK, STANDARD;

    companion object {
        fun toAppIntegrationInfo(iIntegrationInfo: IIntegrationInfo): AppIntegrationInfo =
            when (iIntegrationInfo) {
                is IOdkIntegrationInfo -> ODK
                is IStandardIntegrationInfo -> STANDARD
                else -> throw IllegalArgumentException("Invalid AppIntegrationInfo")
            }
    }
}

