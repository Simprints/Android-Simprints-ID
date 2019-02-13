package com.simprints.id.data.analytics.crashes

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.simprints.id.data.analytics.crashes.CrashlyticsKeysConstants.Companion.FINGERS_SELECTED
import com.simprints.id.data.analytics.crashes.CrashlyticsKeysConstants.Companion.MODULE_IDS
import com.simprints.id.data.analytics.crashes.CrashlyticsKeysConstants.Companion.PEOPLE_DOWN_SYNC_TRIGGERS
import com.simprints.id.data.analytics.crashes.CrashlyticsKeysConstants.Companion.PROJECT_ID
import com.simprints.id.data.analytics.crashes.CrashlyticsKeysConstants.Companion.SESSION_ID
import com.simprints.id.data.analytics.crashes.CrashlyticsKeysConstants.Companion.USER_ID
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.SimprintsException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.libsimprints.FingerIdentifier

class CrashReportManagerImpl: CrashReportManager {

    override fun logInfo(crashReportTag: CrashReportTags, crashTrigger: CrashTrigger, message: String) {
        Crashlytics.log(Log.VERBOSE, crashReportTag.name, getLogMessage(crashTrigger, message))
    }

    override fun logWarning(crashReportTag: CrashReportTags, crashTrigger: CrashTrigger, message: String) {
        Crashlytics.log(Log.WARN, crashReportTag.name, getLogMessage(crashTrigger, message))
    }

    override fun logAlert(alertType: ALERT_TYPE) {
        Crashlytics.log(Log.ERROR, CrashReportTags.ALERT.name, alertType.name)
    }

    private fun getLogMessage(crashPrompter: CrashTrigger, message: String) = "[${crashPrompter.name}] $message"

    override fun logThrowable(throwable: Throwable) {
        if(throwable !is SimprintsException) {
            logException(throwable)
        } else {
            logSafeException(throwable)
        }
    }

    override fun logException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }

    private fun logSafeException(simprintsException: SimprintsException) {
        Crashlytics.log(Log.ERROR, CrashReportTags.SAFE_EXCEPTION.name, "$simprintsException")
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
