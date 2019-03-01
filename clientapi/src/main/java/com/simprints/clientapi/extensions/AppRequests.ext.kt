package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.clientapi.BuildConfig
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Constants.SIMPRINTS_PACKAGE_NAME
import com.simprints.moduleinterfaces.app.confirmations.IAppConfirmation
import com.simprints.moduleinterfaces.app.requests.IAppRequest


fun IAppRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(IAppRequest.BUNDLE_KEY, this@toIntent)
    val packageName = if (BuildConfig.DEBUG) {"${Constants.SIMPRINTS_PACKAGE_NAME}.debug"} else { SIMPRINTS_PACKAGE_NAME }
    setPackage(SIMPRINTS_PACKAGE_NAME)
}

fun IAppConfirmation.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(IAppConfirmation.BUNDLE_KEY, this@toIntent)
    val packageName = if (BuildConfig.DEBUG) {"${Constants.SIMPRINTS_PACKAGE_NAME}.debug"} else { SIMPRINTS_PACKAGE_NAME }
    setPackage(SIMPRINTS_PACKAGE_NAME)
}
