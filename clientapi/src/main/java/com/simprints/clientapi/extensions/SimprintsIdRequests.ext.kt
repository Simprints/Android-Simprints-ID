package com.simprints.clientapi.extensions

import android.content.Intent
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.libsimprints.Constants


fun SimprintsIdRequest.toIntent(action: String): Intent = Intent(action).apply {
    putExtra(this@toIntent.requestName, this@toIntent)
    setPackage(Constants.SIMPRINTS_PACKAGE_NAME)
}
