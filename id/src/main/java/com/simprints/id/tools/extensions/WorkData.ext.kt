package com.simprints.id.tools.extensions

import androidx.work.Data
import com.google.gson.Gson
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation

fun Data.parseAppConfirmation(): AppIdentityConfirmationRequest {
    getString(IAppConfirmation.BUNDLE_KEY)?.let { requestJson ->
        return Gson().fromJson(requestJson, AppIdentityConfirmationRequest::class.java)
    } ?: throw InvalidAppRequest()
}
