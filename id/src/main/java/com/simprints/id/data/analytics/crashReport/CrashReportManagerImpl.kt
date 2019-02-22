package com.simprints.id.data.analytics.crashReport

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.simprints.id.data.analytics.crashReport.CrashlyticsKeyConstants.Companion.FINGERS_SELECTED
import com.simprints.id.data.analytics.crashReport.CrashlyticsKeyConstants.Companion.MODULE_IDS
import com.simprints.id.data.analytics.crashReport.CrashlyticsKeyConstants.Companion.PEOPLE_DOWN_SYNC_TRIGGERS
import com.simprints.id.data.analytics.crashReport.CrashlyticsKeyConstants.Companion.PROJECT_ID
import com.simprints.id.data.analytics.crashReport.CrashlyticsKeyConstants.Companion.SESSION_ID
import com.simprints.id.data.analytics.crashReport.CrashlyticsKeyConstants.Companion.USER_ID
import com.simprints.id.exceptions.safe.SafeException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.libsimprints.FingerIdentifier

class CrashReportManagerImpl: CrashReportManager {

    override fun logMessageForCrashReport(crashReportTag: CrashReportTags, crashTrigger: CrashTrigger,
                                          crashPriority: Int, message: String) {
        Crashlytics.log(crashPriority, crashReportTag.name, getLogMessage(crashTrigger, message))
    }

    private fun getLogMessage(crashTrigger: CrashTrigger, message: String) = "[${crashTrigger.name}] $message"

    override fun logExceptionOrThrowable(throwable: Throwable) {
        if(throwable is SafeException) {
            logSafeException(throwable)
        } else {
            Crashlytics.logException(throwable)
        }
    }

    private fun logSafeException(throwable: Throwable) {
        Crashlytics.log(Log.ERROR, CrashReportTags.SAFE_EXCEPTION.name, "$throwable")
    }

    override fun setProjectIdCrashlyticsKey(projectId: String) {
        Crashlytics.setString(PROJECT_ID, projectId)
    }

    override fun setUserIdCrashlyticsKey(userId: String) {
        Crashlytics.setString(USER_ID, userId)
        Crashlytics.setUserIdentifier(userId)
    }

    override fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?) {
        Crashlytics.setString(MODULE_IDS, moduleIds.toString())
    }

    override fun setDownSyncTriggersCrashlyticsKey(peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>) {
        Crashlytics.setString(PEOPLE_DOWN_SYNC_TRIGGERS, peopleDownSyncTriggers.toString())
    }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
        Crashlytics.setString(SESSION_ID, sessionId)
    }

    override fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>) {
        Crashlytics.setString(FINGERS_SELECTED, fingersSelected.toString())
    }
}
