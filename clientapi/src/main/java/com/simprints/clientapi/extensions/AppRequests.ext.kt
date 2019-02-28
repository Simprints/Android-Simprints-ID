package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.libsimprints.Constants
import com.simprints.moduleinterfaces.app.confirmations.AppConfirmation
import com.simprints.moduleinterfaces.app.requests.AppRequest


fun AppRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(AppRequest.BUNDLE_KEY, this@toIntent)
    setPackage(Constants.SIMPRINTS_PACKAGE_NAME)
}

fun AppConfirmation.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(AppConfirmation.BUNDLE_KEY, this@toIntent)
    setPackage(Constants.SIMPRINTS_PACKAGE_NAME)
}
