package com.simprints.infra.enrolment.records.repository.commcare

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import com.simprints.core.AvailableProcessors
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.enrolment.records.repository.CandidateRecordDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.CandidateRecordBatch
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.usecases.CompareImplicitTokenizedStringsUseCase
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.FaceReference
import com.simprints.infra.events.event.domain.models.FingerprintReference
import com.simprints.infra.logging.Simber
import com.simprints.infra.serialization.SimJson
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.simprints.core.domain.reference.BiometricReference as CoreBiometricReference

internal class CommCareCandidateRecordDataSource @Inject constructor(
    private val timeHelper: TimeHelper,
    private val encoder: EncodingUtils,
    private val compareImplicitTokenizedStringsUseCase: CompareImplicitTokenizedStringsUseCase,
    private val extractCommCareCaseId: ExtractCommCareCaseIdUseCase,
    @param:AvailableProcessors private val availableProcessors: Int,
    @param:ApplicationContext private val context: Context,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
) : CandidateRecordDataSource {
    private fun getCaseMetadataUri(packageName: String): Uri = "content://$packageName.case/casedb/case".toUri()

    private fun getCaseDataUri(packageName: String): Uri = "content://$packageName.case/casedb/data".toUri()

    override suspend fun loadCandidateRecords(
        query: EnrolmentRecordQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<CandidateRecordBatch> = loadIdentitiesConcurrently(
        ranges = ranges,
        scope = scope,
    ) { range ->
        val startTime = timeHelper.now()
        val identities = loadIdentities(
            query = query,
            range = range,
            project = project,
            dataSource = dataSource,
            onCandidateLoaded = onCandidateLoaded,
        )
        val endTime = timeHelper.now()
        CandidateRecordBatch(identities, startTime, endTime)
    }

    private fun loadIdentitiesConcurrently(
        ranges: List<IntRange>,
        scope: CoroutineScope,
        load: suspend (IntRange) -> CandidateRecordBatch,
    ): ReceiveChannel<CandidateRecordBatch> {
        val channel = Channel<CandidateRecordBatch>(availableProcessors)
        val semaphore = Semaphore(availableProcessors)
        scope.launch(dispatcher) {
            ranges
                .map { range ->
                    async { semaphore.withPermit { channel.send(load(range)) } }
                }.joinAll()
            channel.close()
        }
        return channel
    }

    private suspend fun loadIdentities(
        query: EnrolmentRecordQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: suspend () -> Unit,
    ): List<CandidateRecord> = loadEnrolmentRecordCreationEvents(range, dataSource.callerPackageName(), query, project, onCandidateLoaded)
        .filter { erce -> erce.payload.biometricReferences.any { it.format == query.format } }
        .map { erce ->
            CandidateRecord(
                erce.payload.subjectId,
                erce.payload.biometricReferences.mapNotNull { reference ->
                    if (reference.format != query.format) {
                        null
                    } else {
                        when (reference) {
                            is FaceReference -> CoreBiometricReference(
                                referenceId = reference.id,
                                format = reference.format,
                                modality = Modality.FACE,
                                templates = reference.templates.map {
                                    BiometricTemplate(
                                        template = encoder.base64ToBytes(it.template),
                                    )
                                },
                            )

                            is FingerprintReference -> CoreBiometricReference(
                                referenceId = reference.id,
                                format = reference.format,
                                modality = Modality.FINGERPRINT,
                                templates = reference.templates.map {
                                    BiometricTemplate(
                                        identifier = it.finger,
                                        template = encoder.base64ToBytes(it.template),
                                    )
                                },
                            )
                        }
                    }
                },
            )
        }

    private suspend fun loadEnrolmentRecordCreationEvents(
        range: IntRange,
        callerPackageName: String,
        query: EnrolmentRecordQuery,
        project: Project,
        onCandidateLoaded: suspend () -> Unit,
    ): List<EnrolmentRecordCreationEvent> {
        val enrolmentRecordCreationEvents: MutableList<EnrolmentRecordCreationEvent> = mutableListOf()
        try {
            val caseId = extractCommCareCaseId(query.metadata)
            if (caseId != null) {
                return loadEnrolmentRecordCreationEvents(caseId, callerPackageName, query, project)
                    .also { onCandidateLoaded() }
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
                                    .addAll(loadEnrolmentRecordCreationEvents(caseId, callerPackageName, query, project))
                                    .also { onCandidateLoaded() }
                            }
                        } while (caseMetadataCursor.moveToNext() && caseMetadataCursor.position <= range.last)
                    }
                }
        } catch (e: Exception) {
            Simber.e("Error while querying CommCare", e)
        }

        return enrolmentRecordCreationEvents
    }

    private fun loadEnrolmentRecordCreationEvents(
        caseId: String,
        callerPackageName: String,
        query: EnrolmentRecordQuery,
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
        query: EnrolmentRecordQuery,
        event: EnrolmentRecordCreationEvent,
    ): Boolean = query.subjectId == null || query.subjectId == event.payload.subjectId

    private fun isAttendantIdNullOrMatching(
        query: EnrolmentRecordQuery,
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
        query: EnrolmentRecordQuery,
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
            SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(it)
        } catch (e: Exception) {
            Simber.e("Error while parsing subjectActions", e)
            null
        }
    }

    override suspend fun count(
        query: EnrolmentRecordQuery,
        dataSource: BiometricDataSource,
    ): Int = withContext(dispatcher) {
        var count = 0
        val caseId = extractCommCareCaseId(query.metadata)
        if (caseId != null) {
            count = 1
        } else {
            context.contentResolver
                .query(
                    getCaseMetadataUri(dataSource.callerPackageName()),
                    null,
                    null,
                    null,
                    null,
                )?.use { caseMetadataCursor -> count = caseMetadataCursor.count }
        }
        count
    }

    override fun observeCount(
        query: EnrolmentRecordQuery,
        dataSource: BiometricDataSource,
    ): Flow<Int> = callbackFlow {
        val observer = object : ContentObserver(null) {
            /**
             * This relies on CommCare to call notifyChange,
             * like it does
             * for InstanceProvider at https://github.com/dimagi/commcare-android/blob/8f3c950f9de61e4e328989327fbfc015e39c14b0/app/src/org/commcare/provider/InstanceProvider.java#L173
             * and for FormsProvider at https://github.com/dimagi/commcare-android/blob/8f3c950f9de61e4e328989327fbfc015e39c14b0/app/src/org/commcare/provider/FormsProvider.java#L167
             * However, those are the only providers calling notifyChange.
             *
             * CaseDataContentProvider doesn't call notifyChange at least yet in the revision as linked above,
             * so in addition to observing notifyChange, we also poll periodically as a fallback.
             *
             * This implementation still provisions the use of notifyChange in CaseDataContentProvider in CommCare.
             */
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(
            getCaseMetadataUri(dataSource.callerPackageName()),
            true, // notify for descendants
            observer,
        )
        trySend(Unit) // initial count
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.let { contentObserverFlow ->
        merge(
            contentObserverFlow,
            flow {
                while (true) {
                    delay(CASE_COUNT_FALLBACK_POLL_INTERVAL_MILLIS)
                    emit(Unit)
                }
            },
        )
    }.conflate()
        .mapLatest {
            count(query, dataSource)
        }.distinctUntilChanged()

    companion object {
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"

        // Fallback polling interval useful when CaseDataContentProvider does not call notifyChange.
        // A conservative value that avoids excessive load on CommCare
        // while still allowing infrequent visual updates.
        internal const val CASE_COUNT_FALLBACK_POLL_INTERVAL_MILLIS = 300_000L
    }
}
