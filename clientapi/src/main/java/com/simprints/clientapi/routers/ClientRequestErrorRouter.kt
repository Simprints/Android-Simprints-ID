package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.ErrorActivity.Companion.MESSAGE_KEY
import org.jetbrains.anko.startActivity


object ClientRequestErrorRouter {

    fun routeClientRequestError(act: Activity, ex: Exception) =
        act.startActivity<ErrorActivity>(MESSAGE_KEY to ex.message)

}
