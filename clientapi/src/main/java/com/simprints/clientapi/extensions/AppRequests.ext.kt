package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.libsimprints.Constants.SIMPRINTS_PACKAGE_NAME
import com.simprints.moduleinterfaces.app.confirmations.IAppConfirmation
import com.simprints.moduleinterfaces.app.requests.IAppRequest


fun IAppRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(IAppRequest.BUNDLE_KEY, this@toIntent)
    setPackage(SIMPRINTS_PACKAGE_NAME)
}

fun IAppConfirmation.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(IAppConfirmation.BUNDLE_KEY, this@toIntent)
    setPackage(SIMPRINTS_PACKAGE_NAME)
}
