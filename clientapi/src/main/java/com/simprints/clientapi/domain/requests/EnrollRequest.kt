package com.simprints.clientapi.domain.requests

import com.simprints.moduleinterfaces.app.requests.IAppEnrollRequest
import com.simprints.moduleinterfaces.app.requests.IAppRequest
import kotlinx.android.parcel.Parcelize


data class EnrollRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppEnrollRequest(
        this.projectId, this.userId, this.moduleId, this.metadata
    )

    @Parcelize
    private data class AppEnrollRequest(
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        override val metadata: String
    ) : IAppEnrollRequest

}


