package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.ErrorActivity.Companion.MESSAGE_KEY
import com.simprints.clientapi.exceptions.InvalidRequestException
import org.jetbrains.anko.startActivity

object ClientRequestErrorRouter {

    fun routeClientRequestError(act: Activity, ex: InvalidRequestException) =
        act.startActivity<ErrorActivity>(MESSAGE_KEY to ex.message)

}
