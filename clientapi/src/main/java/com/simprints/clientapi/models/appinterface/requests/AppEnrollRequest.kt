package com.simprints.clientapi.models.appinterface.requests

import com.simprints.moduleinterfaces.app.requests.IAppEnrollRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppEnrollRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppEnrollRequest
