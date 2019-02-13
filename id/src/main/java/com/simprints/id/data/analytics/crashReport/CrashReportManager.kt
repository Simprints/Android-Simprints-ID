package com.simprints.id.data.analytics.crashReport

import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.libsimprints.FingerIdentifier

interface CrashReportManager {

    fun logInfo(crashReportTag: CrashReportTags, crashTrigger: CrashTrigger, message: String)
    fun logWarning(crashReportTag: CrashReportTags, crashTrigger: CrashTrigger, message: String)

    fun logAlert(alertType: ALERT_TYPE)
    fun logException(throwable: Throwable)
    fun logThrowable(throwable: Throwable)

    fun setProjectIdCrashlyticsKey(projectId: String)
    fun setUserIdCrashlyticsKey(userId: String)
    fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?)
    fun setDownSyncTriggersCrashlyticsKey(peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>)
    fun setSessionIdCrashlyticsKey(sessionId: String)
    fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>)
}
