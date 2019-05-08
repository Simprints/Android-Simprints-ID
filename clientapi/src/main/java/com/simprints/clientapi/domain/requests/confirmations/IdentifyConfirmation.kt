package com.simprints.clientapi.domain.requests.confirmations

import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.moduleapi.app.requests.IExtraRequestInfo
import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation
import kotlinx.android.parcel.Parcelize


data class IdentifyConfirmation(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String,
    override val extra: ExtraRequestInfo) : BaseConfirmation {

    override fun convertToAppRequest(): IAppConfirmation = AppIdentifyConfirmation(
        this.projectId, this.sessionId, this.selectedGuid, this.extra.toAppRequest()
    )

    @Parcelize
    data class AppIdentifyConfirmation(
        override val projectId: String,
        override val sessionId: String,
        override val selectedGuid: String,
        override val extra: IExtraRequestInfo
    ) : IAppIdentifyConfirmation

}


