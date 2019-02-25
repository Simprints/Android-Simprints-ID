package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.libsimprints.Constants


fun ClientApiBaseRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(this@toIntent.bundleKey, this@toIntent)
    setPackage(Constants.SIMPRINTS_PACKAGE_NAME)
}
