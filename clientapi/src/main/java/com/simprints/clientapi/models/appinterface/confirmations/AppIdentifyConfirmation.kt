package com.simprints.clientapi.models.appinterface.confirmations

import com.simprints.moduleinterfaces.app.confirmations.IAppIdentifyConfirmation
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppIdentifyConfirmation(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : IAppIdentifyConfirmation
