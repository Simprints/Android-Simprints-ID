package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.clientapi.BuildConfig
import com.simprints.moduleapi.app.requests.IAppRequest


fun IAppRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(IAppRequest.BUNDLE_KEY, this@toIntent)
    setPackage(BuildConfig.LIBRARY_PACKAGE_NAME)
}
