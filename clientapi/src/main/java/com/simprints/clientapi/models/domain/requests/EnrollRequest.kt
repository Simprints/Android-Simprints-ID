package com.simprints.clientapi.models.domain.requests

import com.simprints.clientapi.models.appinterface.requests.AppEnrollRequest
import com.simprints.moduleinterfaces.app.requests.IAppRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class EnrollRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppEnrollRequest(
        this.projectId, this.userId, this.moduleId, this.metadata
    )

}


