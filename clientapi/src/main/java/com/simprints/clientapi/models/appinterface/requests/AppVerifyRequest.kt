package com.simprints.clientapi.models.appinterface.requests

import com.simprints.moduleinterfaces.app.requests.IAppVerifyRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppVerifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val verifyGuid: String
) : IAppVerifyRequest
