package com.simprints.clientapi.routers

import android.content.Context
import android.content.Intent
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException


object ClientRequestErrorRouter {

    fun getIntentForError(context: Context, ex: Exception): Intent = when (ex) {
        is InvalidProjectIdException -> Intent(context, OdkActivity::class.java)
        is InvalidUserIdException -> Intent(context, OdkActivity::class.java)
        is InvalidModuleIdException -> Intent(context, OdkActivity::class.java)
        is InvalidMetadataException -> Intent(context, OdkActivity::class.java)
        else -> Intent(context, OdkActivity::class.java)
    }

}
