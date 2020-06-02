package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.libsimprints.Constants.SIMPRINTS_PACKAGE_NAME
import com.simprints.moduleapi.app.requests.IAppRequest


fun IAppRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(IAppRequest.BUNDLE_KEY, this@toIntent)
    setPackage(SIMPRINTS_PACKAGE_NAME)
}
