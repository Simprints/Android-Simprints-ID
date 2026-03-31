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

    companion object {
        const val SESSION_CLOSED_ACTION = "com.simprints.id.broadcasts.SESSION_CLOSED_ACTION"
    }
}
