package com.simprints.id.tools.extensions

import android.content.Intent
import com.simprints.id.domain.requests.*
import com.simprints.moduleinterfaces.app.confirmations.IAppIdentifyConfirmation
import com.simprints.moduleinterfaces.app.requests.IAppEnrollRequest
import com.simprints.moduleinterfaces.app.requests.IAppIdentifyRequest
import com.simprints.moduleinterfaces.app.requests.IAppRequest
import com.simprints.moduleinterfaces.app.requests.IAppVerifyRequest

fun Intent.parseClientApiRequest(): BaseRequest =
    this.extras?.let {
        with(it.getParcelable<IAppRequest>(IAppRequest.BUNDLE_KEY)) {
            when (this) {
                is IAppEnrollRequest -> EnrolRequest(this)
                is IAppVerifyRequest -> VerifyRequest(this)
                is IAppIdentifyRequest -> IdentifyRequest(this)
                is IAppIdentifyConfirmation -> IdentityConfirmationRequest(this)
                else -> null
            }
        }
    } ?: throw Throwable("no extra") //StopShip
