package com.simprints.clientapi.domain.requests

import android.os.Parcelable
import com.simprints.moduleapi.app.requests.IExtraRequestInfo
import com.simprints.moduleapi.app.requests.IIntegrationInfo
import kotlinx.android.parcel.Parcelize


data class ExtraRequestInfo(val integration: IntegrationInfo) {

    fun toAppRequest(): IExtraRequestInfo = ExtraRequestInfo(integration.toAppIntegrationInfo())

    @Parcelize
    private data class ExtraRequestInfo(override val integration: IIntegrationInfo)
        : IExtraRequestInfo, Parcelable

}
