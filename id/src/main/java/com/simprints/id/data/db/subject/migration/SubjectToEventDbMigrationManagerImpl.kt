package com.simprints.id.data.db.subject.migration

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MigrationToNewEventArchitectureException
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.flow.toList
import timber.log.Timber

class SubjectToEventDbMigrationManagerImpl(
    val loginInfoManager: LoginInfoManager,
    private val eventLocal: EventLocalDataSource,
    val timeHelper: TimeHelper,
    val crashReportManager: CrashReportManager,
    val preferencesManager: PreferencesManager,
    private val subjectLocal: SubjectLocalDataSource) : SubjectToEventMigrationManager {

    override suspend fun migrateSubjectToSyncToEventsDb() {
        try {
            if (loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty()) {
                val subjectsToSync = subjectLocal.load(SubjectQuery(toSync = true)).toList()
                if (subjectsToSync.isNotEmpty()) {

                    for (subject in subjectsToSync) {
                        // The repo is not used because when an events are added,
                        // labels are set based on the current state (e.g labels.projectId = signedInProject)
                        // In this case, the event needs to be generated off from the subject
                        // as the subject is stored in the old db.
                        eventLocal.insertOrUpdate(
                            fromSubjectToEnrolmentCreationEvent(subject)
                        )
                    }

                    markSubjectAsSynced(subjectsToSync)
                }
            }
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logException(MigrationToNewEventArchitectureException(cause = t))
        }
    }

    private fun fromSubjectToEnrolmentCreationEvent(subject: Subject): EnrolmentRecordCreationEvent {
        return EnrolmentRecordCreationEvent(
            timeHelper.now(),
            subject.subjectId,
            subject.projectId,
            subject.moduleId,
            subject.attendantId,
            preferencesManager.modalities.map { it.toMode() },
            EnrolmentRecordCreationEvent.buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
        )
    }

    private suspend fun markSubjectAsSynced(subjectsToSync: List<Subject>) {
        subjectLocal.performActions(subjectsToSync.map {
            //It overrides the original subject since the subjectId is the same
            Creation(it.copy(toSync = false))
        })
    }
}
