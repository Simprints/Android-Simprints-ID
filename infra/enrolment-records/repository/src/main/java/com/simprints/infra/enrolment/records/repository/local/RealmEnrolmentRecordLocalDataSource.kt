package com.simprints.infra.enrolment.records.repository.local

import com.simprints.core.DispatcherIO
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.models.toDomain
import com.simprints.infra.enrolment.records.repository.local.models.toRealmDb
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RealmEnrolmentRecordLocalDataSource @Inject constructor(
    private val realmWrapper: RealmWrapper,
    private val tokenizationProcessor: TokenizationProcessor,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : EnrolmentRecordLocalDataSource {
    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "attendantId"
        const val SUBJECT_ID_FIELD = "subjectId"
        const val MODULE_ID_FIELD = "moduleId"
        const val IS_ATTENDANT_ID_TOKENIZED_FIELD = "isAttendantIdTokenized"
        const val IS_MODULE_ID_TOKENIZED_FIELD = "isModuleIdTokenized"
        const val FINGERPRINT_SAMPLES_FIELD = "fingerprintSamples"
        const val FACE_SAMPLES_FIELD = "faceSamples"
        const val FORMAT_FIELD = "format"

        // Although batches are processed sequentially, we use a small channel capacity to prevent blocking and reduce the risk of out-of-memory errors.
        const val CHANNEL_CAPACITY = 4
    }

    override suspend fun load(query: SubjectQuery): List<Subject> = realmWrapper.readRealm {
        it
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find()
            .map { dbSubject -> dbSubject.toDomain() }
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<List<FaceIdentity>> {
        val channel = Channel<List<FaceIdentity>>(CHANNEL_CAPACITY)
        scope.launch(dispatcherIO) {
            ranges.forEach { range ->
                val identities = loadIdentitiesRange(
                    query = query,
                    range = range,
                    mapper = { dbSubject ->
                        FaceIdentity(
                            subjectId = dbSubject.subjectId.toString(),
                            faces = dbSubject.faceSamples
                                .filter { it.format == query.faceSampleFormat }
                                .map { it.toDomain() },
                        )
                    },
                    onCandidateLoaded = onCandidateLoaded,
                )
                channel.send(identities)
            }
            channel.close()
        }
        return channel
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<List<FingerprintIdentity>> {
        val channel = Channel<List<FingerprintIdentity>>(CHANNEL_CAPACITY)
        scope.launch(dispatcherIO) {
            ranges.forEach { range ->
                val identities = loadIdentitiesRange(
                    query = query,
                    range = range,
                    mapper = { dbSubject ->
                        FingerprintIdentity(
                            subjectId = dbSubject.subjectId.toString(),
                            fingerprints = dbSubject.fingerprintSamples
                                .filter { it.format == query.fingerprintSampleFormat }
                                .map { it.toDomain() },
                        )
                    },
                    onCandidateLoaded = onCandidateLoaded,
                )
                channel.send(identities)
            }
            channel.close()
        }
        return channel
    }

    private suspend fun <T> loadIdentitiesRange(
        query: SubjectQuery,
        range: IntRange,
        mapper: (DbSubject) -> T,
        onCandidateLoaded: suspend () -> Unit,
    ): List<T> = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            // subList's second parameter is exclusive, so we need to add 1 to the last index
            .find { it.subList(range.first, range.last + 1) }
            .map { dbSubject ->
                onCandidateLoaded()
                mapper(dbSubject)
            }
    }

    override suspend fun delete(queries: List<SubjectQuery>) {
        realmWrapper.writeRealm { realm ->
            queries.forEach {
                realm.delete(realm.query(DbSubject::class).buildRealmQueryForSubject(it))
            }
        }
    }

    override suspend fun deleteAll() {
        realmWrapper.writeRealm { realm ->
            realm.deleteAll()
        }
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .count()
            .find()
            .toInt()
    }

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) {
        // if there is no actions to perform return to avoid useless realm operations
        if (actions.isEmpty()) {
            Simber.d("[SubjectLocalDataSourceImpl] No realm actions to perform", tag = REALM_DB)
            return
        }

        realmWrapper.writeRealm { realm ->
            actions.forEach { action ->
                when (action) {
                    is SubjectAction.Creation -> {
                        val newSubject = action.subject
                            .copy(
                                moduleId =
                                    tokenizationProcessor.tokenizeIfNecessary(
                                        action.subject.moduleId,
                                        TokenKeyType.ModuleId,
                                        project,
                                    ),
                                attendantId = tokenizationProcessor.tokenizeIfNecessary(
                                    action.subject.attendantId,
                                    TokenKeyType.AttendantId,
                                    project,
                                ),
                            ).toRealmDb()
                        val dbSubject: DbSubject? = realm.findSubject(newSubject.subjectId)

                        if (dbSubject != null) {
                            // When updating an existing subject, we must manually delete outdated samples
                            val fingerprintSampleIds = newSubject.fingerprintSamples.map { it.id }.toSet()
                            dbSubject.fingerprintSamples
                                .filterNot { it.id in fingerprintSampleIds }
                                .takeIf { it.isNotEmpty() }
                                ?.forEach { realm.delete(it) }

                            val faceSampleIds = newSubject.faceSamples.map { it.id }.toSet()
                            dbSubject.faceSamples
                                .filterNot { it.id in faceSampleIds }
                                .takeIf { it.isNotEmpty() }
                                ?.forEach { realm.delete(it) }

                            val externalCredentialIds = newSubject.externalCredentials.map { it.id }.toSet()
                            dbSubject.externalCredentials
                                .filterNot { it.id in externalCredentialIds }
                                .takeIf { it.isNotEmpty() }
                                ?.forEach { realm.delete(it) }
                        }

                        realm.copyToRealm(newSubject, updatePolicy = UpdatePolicy.ALL)
                    }

                    is SubjectAction.Update -> {
                        val dbSubject: DbSubject? = realm.findSubject(RealmUUID.from(action.subjectId))
                        if (dbSubject != null) {
                            val referencesToDelete = action.referenceIdsToRemove.toSet() // to make lookup O(1)
                            val faceSamplesMap = dbSubject.faceSamples.groupBy { it.referenceId in referencesToDelete }
                            val fingerprintSamplesMap = dbSubject.fingerprintSamples.groupBy { it.referenceId in referencesToDelete }
                            val allExternalCredentials = (dbSubject.externalCredentials + action.externalCredentialsToAdd.map { it.toRealmDb() }).distinctBy { it.id }.toSet()

                            // Append new samples to the list of samples that remain after removing
                            dbSubject.faceSamples = (
                                faceSamplesMap[false].orEmpty() + action.faceSamplesToAdd.map { it.toRealmDb() }
                                ).toRealmList()
                            dbSubject.fingerprintSamples = (
                                fingerprintSamplesMap[false].orEmpty() + action.fingerprintSamplesToAdd.map { it.toRealmDb() }
                                ).toRealmList()
                            dbSubject.externalCredentials = allExternalCredentials.toRealmList()

                            faceSamplesMap[true]?.forEach { realm.delete(it) }
                            fingerprintSamplesMap[true]?.forEach { realm.delete(it) }
                            realm.copyToRealm(dbSubject, updatePolicy = UpdatePolicy.ALL)
                        } else {
                            Simber.i("[SubjectLocalDataSourceImpl] Subject not found for update", tag = REALM_DB)
                        }
                    }

                    is SubjectAction.Deletion -> realm.delete(
                        realm
                            .query(DbSubject::class)
                            .buildRealmQueryForSubject(query = SubjectQuery(subjectId = action.subjectId))
                            .find(),
                    )
                }
            }
        }
    }

    override suspend fun getLocalDBInfo() = realmWrapper.readRealm { realm ->
        val dbName = realm.configuration.name
        val dbVersion = realm.configuration.schemaVersion
        val subjectCount = realm
            .query(DbSubject::class)
            .count()
            .find()
            .toInt()
        val dbSize = File(realm.configuration.path).length() / (1024.0)
        "Realm DB Info:\n" +
            "Database Name: ${dbName}\n" +
            "Database Version: $dbVersion\n" +
            "Database Path: ${realm.configuration.path}\n" +
            "Database Size: $dbSize KB\n" +
            "Number of Subjects: $subjectCount"
    }

    private fun MutableRealm.findSubject(subjectId: RealmUUID): DbSubject? =
        query(DbSubject::class).query("$SUBJECT_ID_FIELD == $0", subjectId).first().find()

    private fun RealmQuery<DbSubject>.buildRealmQueryForSubject(query: SubjectQuery): RealmQuery<DbSubject> {
        var realmQuery = this

        if (query.projectId != null) {
            realmQuery = realmQuery.query("$PROJECT_ID_FIELD == $0", query.projectId)
        }
        if (query.subjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD == $0",
                RealmUUID.from(query.subjectId),
            )
        }
        if (query.subjectIds != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD IN $0",
                query.subjectIds.map { RealmUUID.from(it) },
            )
        }
        if (query.attendantId != null) {
            realmQuery = realmQuery.query("$USER_ID_FIELD == $0", query.attendantId.value)
        }
        if (query.moduleId != null) {
            realmQuery = realmQuery.query("$MODULE_ID_FIELD == $0", query.moduleId.value)
        }
        if (query.fingerprintSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY $FINGERPRINT_SAMPLES_FIELD.$FORMAT_FIELD == $0",
                query.fingerprintSampleFormat,
            )
        }
        if (query.faceSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY $FACE_SAMPLES_FIELD.$FORMAT_FIELD == $0",
                query.faceSampleFormat,
            )
        }
        if (query.afterSubjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD >= $0",
                RealmUUID.from(query.afterSubjectId),
            )
        }
        if (query.hasUntokenizedFields != null) {
            realmQuery = realmQuery.query(
                "$IS_ATTENDANT_ID_TOKENIZED_FIELD == $0 OR $IS_MODULE_ID_TOKENIZED_FIELD == $1",
                false,
                false,
            )
        }
        if (query.sort) {
            realmQuery = realmQuery.sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
        }
        return realmQuery
    }

    /**
     * Loads all subjects in batches of the specified size.
     */
    fun loadAllSubjectsInBatches(batchSize: Int): Flow<List<Subject>> = channelFlow {
        require(batchSize > 0) {
            "Batch size must be greater than 0"
        }
        realmWrapper.readRealm { realm ->
            launch {
                var query = realm.query(DbSubject::class).sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
                while (true) {
                    val batch = query
                        .find { it.take(batchSize) }
                        .map { it.toDomain() }

                    if (batch.isEmpty()) {
                        break
                    }
                    send(batch)
                    // Update the query to fetch the next batch
                    query = query.query("$SUBJECT_ID_FIELD > $0", RealmUUID.from(batch.last().subjectId))
                }
            }
        }
    }

    override suspend fun getAllSubjectIds(): List<String> = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
            .find()
            .map { it.subjectId.toString() }
    }
}
