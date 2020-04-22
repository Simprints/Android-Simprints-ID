package com.simprints.id.data.db.person

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState.RUNNING
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncProgress
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.*
import com.simprints.id.data.db.person.domain.personevents.Event.Companion.ATTENDANT_ID_LABEL
import com.simprints.id.data.db.person.domain.personevents.Event.Companion.MODE_LABEL
import com.simprints.id.data.db.person.domain.personevents.Event.Companion.MODULE_ID_LABEL
import com.simprints.id.data.db.person.domain.personevents.Event.Companion.PROJECT_ID_LABEL
import com.simprints.id.data.db.person.domain.personevents.Event.Companion.SUBJECT_ID_LABEL
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.toMode
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

class PersonRepositoryUpSyncHelperImpl(
    private val loginInfoManager: LoginInfoManager,
    private val personLocalDataSource: PersonLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
    private val modalities: List<Modality>,
    private val cache: PeopleSyncCache
) : PersonRepositoryUpSyncHelper {

    var count = 0

    val projectId: String
        get() {
            val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
            return if (projectId.isEmpty() /*|| userId != loginInfoManager.signedInUserId*/) {
                throw IllegalStateException("Only people enrolled by the currently signed in user can be up-synced")
            } else {
                projectId
            }
        }

    override suspend fun executeUpload(scope: CoroutineScope) = scope.produce {
        try {
            //count = cache.readProgress(workerId)

            personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true))
                .bufferedChunks(80)
                .collect {
                    upSyncBatch(it)
                    count += it.size
                    this.send(PeopleUpSyncProgress(count))
                }

        } catch (t: Throwable) {
            t.printStackTrace()
            updateState(UpSyncState.FAILED)
            throw t
        }

        updateState(UpSyncState.COMPLETE)
    }

    private suspend fun upSyncBatch(people: List<Person>) {
        uploadPeople(people)
        Timber.d("Uploaded a batch of ${people.size} people")
        markPeopleAsSynced(people)
        Timber.d("Marked a batch of ${people.size} people as synced")
        updateState(RUNNING)
    }

    private suspend fun uploadPeople(people: List<Person>) {
        if (people.isNotEmpty()) {
            eventRemoteDataSource.post(projectId, createEvents(people))
        }
    }

    internal fun createEvents(people: List<Person>) =
        Events(people.map { createEventFromPerson(it) })

    private fun createEventFromPerson(person: Person): Event =
        with(person) {
            Event(
                UUID.randomUUID().toString(),
                createLabels(projectId, moduleId, userId, patientId),
                createPayload(person)
            )
        }


    private fun createLabels(projectId: String, moduleId: String, userId: String, patientId: String) =
        mapOf(
            PROJECT_ID_LABEL to listOf(projectId),
            MODULE_ID_LABEL to listOf(moduleId),
            ATTENDANT_ID_LABEL to listOf(userId),
            SUBJECT_ID_LABEL to listOf(patientId),
            MODE_LABEL to modalities.map { it.toMode().name }
        )

    private fun createPayload(person: Person) =
        EnrolmentRecordCreation(
            subjectId = person.patientId,
            projectId = person.projectId,
            moduleId = person.moduleId,
            attendantId = person.userId,
            biometricReferences = buildBiometricReferences(person.fingerprintSamples, person.faceSamples)
        )

    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>, faceSamples: List<FaceSample>) =
        listOf(
            FingerprintReference(
                fingerprintSamples.map {
                    FingerprintTemplate(it.templateQualityScore,
                        EncodingUtils.byteArrayToBase64(it.template),
                        it.fingerIdentifier.fromPersonToEvent())
                }),
            FaceReference(
                faceSamples.map {
                    FaceTemplate(
                        EncodingUtils.byteArrayToBase64(it.template)
                    )
                })
        )

    private fun markPeopleAsSynced(people: List<Person>) {
        val updatedPeople = people.map { it.copy(toSync = false) }
        runBlocking { personLocalDataSource.insertOrUpdate(updatedPeople) }
    }

    private suspend fun updateLastUpSyncTime(peopleUpSyncOperation: PeopleUpSyncOperation) {
        peopleUpSyncScopeRepository.insertOrUpdate(peopleUpSyncOperation)
    }

    private suspend fun updateState(state: UpSyncState) {
        updateLastUpSyncTime(PeopleUpSyncOperation(
            projectId,
            PeopleUpSyncOperationResult(
                state,
                Date().time
            )
        ))
    }
}
