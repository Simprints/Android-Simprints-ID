package com.simprints.id.data.db.subject.migration

import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.domain.modality.toMode
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.exceptions.unexpected.MigrationToNewEventArchitectureException
import com.simprints.logging.Simber
import kotlinx.coroutines.flow.toList

@Deprecated("To be removed once 2020.3.2 is not supported anymore.")
/**
 * Since 2020.4.0, events are up-synced, not subjects as it was before.
 * Thus, when SID gets updates to 2020.4.0, the subjects in the 2020.3.2 db ready for up-synced
 * need to be converted into events, so they can be up-synced from 2020.4.0.
 * When 2020.3.2 gets unsupported, SubjectToEventDbMigrationManager and Subject.toSync can be removed.
 * */
class SubjectToEventDbMigrationManagerImpl(
    val loginInfoManager: LoginInfoManager,
    private val eventLocal: EventLocalDataSource,
    val timeHelper: TimeHelper,
    val crashReportManager: CrashReportManager,
    val preferencesManager: IdPreferencesManager,
    private val subjectLocal: SubjectLocalDataSource,
    private val encoder: EncodingUtils
) : SubjectToEventMigrationManager {

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
            Simber.e(t)
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
            EnrolmentRecordCreationEvent.buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples, encoder)
        )
    }

    private suspend fun markSubjectAsSynced(subjectsToSync: List<Subject>) {
        subjectLocal.performActions(subjectsToSync.map {
            //It overrides the original subject since the subjectId is the same
            Creation(it.copy(toSync = false))
        })
    }
}
