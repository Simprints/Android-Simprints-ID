package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.IExtraRequestInfo
import kotlinx.android.parcel.Parcelize


data class EnrollRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String,
    override val extra: ExtraRequestInfo
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppEnrollRequest(
        this.projectId, this.userId, this.moduleId, this.metadata, this.extra.toAppRequest()
    )

    @Parcelize
    private data class AppEnrollRequest(
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        override val metadata: String,
        override val extra: IExtraRequestInfo
    ) : IAppEnrollRequest
}


