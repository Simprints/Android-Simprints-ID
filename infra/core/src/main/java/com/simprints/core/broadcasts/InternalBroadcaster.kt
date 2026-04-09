package com.simprints.core.broadcasts

import android.content.Context
import android.content.Intent
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Wrapper for intent broadcast sending")
class InternalBroadcaster @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun sessionClosed() {
        context.sendBroadcast(Intent(SESSION_CLOSED_ACTION).setPackage(context.packageName))
    }

    fun loggedOut(isProjectEnded: Boolean) {
        context.sendBroadcast(
            Intent(LOGOUT_ACTION)
                .setPackage(context.packageName)
                .putExtra(LOGOUT_EXTRA_IS_PROJECT_ENDED, isProjectEnded),
        )
    }

    companion object {
        const val SESSION_CLOSED_ACTION = "com.simprints.id.broadcasts.SESSION_CLOSED_ACTION"

        const val LOGOUT_ACTION = "com.simprints.id.broadcasts.LOGOUT_ACTION"
        const val LOGOUT_EXTRA_IS_PROJECT_ENDED = "is_project_ended"
    }
}
