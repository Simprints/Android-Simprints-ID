package com.simprints.id.tools.extensions

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.AppRequestToDomainRequest.fromAppToDomainRequest
import com.simprints.id.domain.moduleapi.app.requests.AppBaseRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation

fun Intent.parseAppRequest(): AppBaseRequest =
    this.extras?.getParcelable<IAppRequest>(IAppRequest.BUNDLE_KEY)?.let {
        fromAppToDomainRequest(it)
    } ?: throw Throwable("no extra") //StopShip

fun Intent.parseAppConfirmation(): AppBaseRequest =
    this.extras?.getParcelable<IAppRequest>(IAppConfirmation.BUNDLE_KEY)?.let {
    fromAppToDomainRequest(it)
} ?: throw Throwable("no extra") //StopShip
