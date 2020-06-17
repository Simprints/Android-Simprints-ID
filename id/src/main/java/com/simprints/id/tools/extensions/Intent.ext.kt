package com.simprints.id.tools.extensions

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.fromModuleApiToDomain
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.moduleapi.app.requests.IAppRequest

fun Intent.parseAppRequest(): AppRequest =
    this.extras?.getParcelable<IAppRequest>(IAppRequest.BUNDLE_KEY)?.let {
        it.fromModuleApiToDomain()
    } ?: throw InvalidAppRequest()
