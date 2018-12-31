package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.ErrorActivity.Companion.MESSAGE_KEY
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException
import org.jetbrains.anko.startActivity


object ClientRequestErrorRouter {

    fun routeClientRequestError(act: Activity, ex: Exception) = when (ex) {
        is InvalidProjectIdException -> callErrorActivity(act, R.string.invalid_projectId_message)
        is InvalidUserIdException -> callErrorActivity(act, R.string.invalid_userId_message)
        is InvalidModuleIdException -> callErrorActivity(act, R.string.invalid_moduleId_message)
        is InvalidMetadataException -> callErrorActivity(act, R.string.invalid_metadata_message)
        else -> callErrorActivity(act, R.string.unexpected_parameter_message)
    }

    private fun callErrorActivity(act: Activity, messageId: Int) =
        act.startActivity<ErrorActivity>(MESSAGE_KEY to act.getString(messageId))

}
