package com.simprints.infra.enrolment.records.repository.commcare

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.AvailableProcessors
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.enrolment.records.repository.IdentityDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.usecases.CompareImplicitTokenizedStringsUseCase
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordCreationEventDeserializer
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.logging.Simber
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONException
import javax.inject.Inject

internal class CommCareIdentityDataSource @Inject constructor(
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
    private val compareImplicitTokenizedStringsUseCase: CompareImplicitTokenizedStringsUseCase,
    @AvailableProcessors private val availableProcessors: Int,
    @ApplicationContext private val context: Context,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : IdentityDataSource {
    private fun getCaseMetadataUri(packageName: String): Uri = "content://$packageName.case/casedb/case".toUri()

    private fun getCaseDataUri(packageName: String): Uri = "content://$packageName.case/casedb/data".toUri()

    private suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: suspend () -> Unit,
    ): List<FingerprintIdentity> =
        loadEnrolmentRecordCreationEvents(range, dataSource.callerPackageName(), query, project, onCandidateLoaded)
            .filter { erce ->
                erce.payload.biometricReferences.any { it is FingerprintReference && it.format == query.fingerprintSampleFormat }
            }.map {
                FingerprintIdentity(
                    it.payload.subjectId,
                    it.payload.biometricReferences.filterIsInstance<FingerprintReference>().flatMap { fingerprintReference ->
                        fingerprintReference.templates.map { fingerprintTemplate ->
                            FingerprintSample(
                                fingerIdentifier = fingerprintTemplate.finger,
                                template = encoder.base64ToBytes(fingerprintTemplate.template),
                                format = fingerprintReference.format,
                                referenceId = fingerprintReference.id,
                            )
                        }
                    },
                )
            }

    private suspend fun loadEnrolmentRecordCreationEvents(
        range: IntRange,
        callerPackageName: String,
        query: SubjectQuery,
        project: Project,
        onCandidateLoaded: suspend () -> Unit,
    ): List<EnrolmentRecordCreationEvent> {
        val enrolmentRecordCreationEvents: MutableList<EnrolmentRecordCreationEvent> = mutableListOf()
        try {
            val caseId = attemptExtractingCaseId(query.metadata)
            if (caseId != null) {
                return loadEnrolmentRecordCreationEvents(caseId, callerPackageName, query, project)
            }

            context.contentResolver
                .query(
                    getCaseMetadataUri(callerPackageName),
                    null,
                    null,
                    null,
                    null,
                )?.use { caseMetadataCursor ->
                    if (caseMetadataCursor.moveToPosition(range.first)) {
                        do {
                            caseMetadataCursor.getString(caseMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID))?.let { caseId ->
                                enrolmentRecordCreationEvents
                                    .addAll(
                                        loadEnrolmentRecordCreationEvents(caseId, callerPackageName, query, project),
                                    ).also { onCandidateLoaded() }
                            }
                        } while (caseMetadataCursor.moveToNext() && caseMetadataCursor.position <= range.last)
                    }
                }
        } catch (e: Exception) {
            Simber.e("Error while querying CommCare", e)
        }

        return enrolmentRecordCreationEvents
    }

    private fun attemptExtractingCaseId(metadata: String?) = metadata?.takeUnless { it.isEmpty() }?.let {
        try {
            JsonHelper.fromJson<Map<String, Any>>(it)[ARG_CASE_ID] as? String
        } catch (_: JSONException) {
            null
        }
    }

    private suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: suspend () -> Unit,
    ): List<FaceIdentity> = loadEnrolmentRecordCreationEvents(range, dataSource.callerPackageName(), query, project, onCandidateLoaded)
        .filter { erce ->
            erce.payload.biometricReferences.any { it is FaceReference && it.format == query.faceSampleFormat }
        }.map {
            FaceIdentity(
                it.payload.subjectId,
                it.payload.biometricReferences.filterIsInstance<FaceReference>().flatMap { faceReference ->
                    faceReference.templates.map { faceTemplate ->
                        FaceSample(
                            template = encoder.base64ToBytes(faceTemplate.template),
                            format = faceReference.format,
                            referenceId = faceReference.id,
                        )
                    }
                },
            )
        }

    private fun loadEnrolmentRecordCreationEvents(
        caseId: String,
        callerPackageName: String,
        query: SubjectQuery,
        project: Project,
    ): List<EnrolmentRecordCreationEvent> {
        // Access Case Data Listing for the caseId
        val caseDataUri = getCaseDataUri(callerPackageName).buildUpon().appendPath(caseId).build()

        return context.contentResolver
            .query(caseDataUri, null, null, null, null)
            ?.use { caseDataCursor ->
                val subjectActions = getSubjectActionsValue(caseDataCursor)
                Simber.d(subjectActions)
                val coSyncEnrolmentRecordEvents = parseRecordEvents(subjectActions)

                coSyncEnrolmentRecordEvents?.events?.filterIsInstance<EnrolmentRecordCreationEvent>()?.filter { event ->
                    // [MS-852] Plain strings from CommCare might be tokenized or untokenized. The only way to properly compare them
                    // is by trying to decrypt the values to check if already tokenized, and then compare the values
                    isSubjectIdNullOrMatching(query, event) &&
                        isAttendantIdNullOrMatching(
                            query,
                            event,
                            project,
                        ) &&
                        isModuleIdNullOrMatching(query, event, project)
                }
            }.orEmpty()
    }

    private fun isSubjectIdNullOrMatching(
        query: SubjectQuery,
        event: EnrolmentRecordCreationEvent,
    ): Boolean = query.subjectId == null || query.subjectId == event.payload.subjectId

    private fun isAttendantIdNullOrMatching(
        query: SubjectQuery,
        event: EnrolmentRecordCreationEvent,
        project: Project,
    ): Boolean = query.attendantId == null ||
        compareImplicitTokenizedStringsUseCase(
            query.attendantId,
            event.payload.attendantId,
            TokenKeyType.AttendantId,
            project,
        )

    private fun isModuleIdNullOrMatching(
        query: SubjectQuery,
        event: EnrolmentRecordCreationEvent,
        project: Project,
    ): Boolean = query.moduleId == null ||
        compareImplicitTokenizedStringsUseCase(
            query.moduleId,
            event.payload.moduleId,
            TokenKeyType.ModuleId,
            project,
        )

    private fun getSubjectActionsValue(caseDataCursor: Cursor): String {
        while (caseDataCursor.moveToNext()) {
            val key = caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID))
            if (key == SIMPRINTS_COSYNC_SUBJECT_ACTIONS) {
                return caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_VALUE))
            }
        }
        return ""
    }

    private fun parseRecordEvents(subjectActions: String) = subjectActions.takeIf(String::isNotEmpty)?.let {
        try {
            jsonHelper.fromJson<CoSyncEnrolmentRecordEvents>(
                json = it,
                module = coSyncSerializationModule,
                type = object : TypeReference<CoSyncEnrolmentRecordEvents>() {},
            )
        } catch (e: Exception) {
            Simber.e("Error while parsing subjectActions", e)
            null
        }
    }

    private val coSyncSerializationModule = SimpleModule().apply {
        addSerializer(
            TokenizableString::class.java,
            TokenizationClassNameSerializer(),
        )
        addDeserializer(
            TokenizableString::class.java,
            TokenizationClassNameDeserializer(),
        )
        addDeserializer(
            EnrolmentRecordCreationEvent::class.java,
            CoSyncEnrolmentRecordCreationEventDeserializer(),
        )
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcher) {
        var count = 0
        context.contentResolver
            .query(
                getCaseMetadataUri(dataSource.callerPackageName()),
                null,
                null,
                null,
                null,
            )?.use { caseMetadataCursor -> count = caseMetadataCursor.count }
        count
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<List<FaceIdentity>> = loadIdentitiesConcurrently(
        ranges = ranges,
        scope = scope,
    ) { range ->
        loadFaceIdentities(
            query = query,
            range = range,
            project = project,
            dataSource = dataSource,
            onCandidateLoaded = onCandidateLoaded,
        )
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<List<FingerprintIdentity>> = loadIdentitiesConcurrently(
        ranges = ranges,
        scope = scope,
    ) { range ->
        loadFingerprintIdentities(
            query = query,
            range = range,
            project = project,
            dataSource = dataSource,
            onCandidateLoaded = onCandidateLoaded,
        )
    }

    fun <T> loadIdentitiesConcurrently(
        ranges: List<IntRange>,
        scope: CoroutineScope,
        load: suspend (IntRange) -> List<T>,
    ): ReceiveChannel<List<T>> {
        val channel = Channel<List<T>>(availableProcessors)
        val semaphore = Semaphore(availableProcessors)
        scope.launch(dispatcher) {
            ranges
                .map { range ->
                    async {
                        semaphore.withPermit {
                            channel.send(load(range))
                        }
                    }
                }.joinAll()
            channel.close()
        }
        return channel
    }

    companion object {
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"
        const val ARG_CASE_ID = "caseId"
    }
}
