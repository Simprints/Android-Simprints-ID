package com.simprints.clientapi.models.appinterface.requests

import com.simprints.moduleinterfaces.app.requests.IAppIdentifyRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppIdentifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppIdentifyRequest
