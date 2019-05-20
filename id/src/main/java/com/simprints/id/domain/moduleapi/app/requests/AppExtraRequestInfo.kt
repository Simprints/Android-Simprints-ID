package com.simprints.id.domain.moduleapi.app.requests

import android.os.Parcelable
import com.simprints.moduleapi.app.requests.IExtraRequestInfo
import com.simprints.moduleapi.app.requests.IIntegrationInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppExtraRequestInfo(
    val integration: AppIntegrationInfo
): Parcelable {

    constructor(iExtraInfo: IExtraRequestInfo) : this(
        integration = AppIntegrationInfo.toAppIntegrationInfo(iExtraInfo.integration)
    )
}
