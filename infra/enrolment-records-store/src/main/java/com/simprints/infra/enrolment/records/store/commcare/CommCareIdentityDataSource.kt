package com.simprints.infra.enrolment.records.store.commcare

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.store.IdentityDataSource
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.logging.Simber
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONException
import javax.inject.Inject

internal class CommCareIdentityDataSource @Inject constructor(
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
    @ApplicationContext private val context: Context,
) : IdentityDataSource {

    companion object {

        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"

        const val ARG_CASE_ID = "caseId"
    }

    private fun getCaseMetadataUri(packageName: String): Uri =
        Uri.parse("content://$packageName.case/casedb/case")

    private fun getCaseDataUri(packageName: String): Uri =
        Uri.parse("content://$packageName.case/casedb/data")

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FingerprintIdentity> = loadEnrolmentRecordCreationEvents(range, dataSource.callerPackageName(), query)
        .filter { erce -> erce.payload.biometricReferences.any { it is FingerprintReference } }
        .map {
            FingerprintIdentity(
                it.payload.subjectId,
                it.payload.biometricReferences.filterIsInstance<FingerprintReference>()
                    .flatMap { fingerprintReference ->
                        fingerprintReference.templates.map { fingerprintTemplate ->
                            FingerprintSample(
                                fingerIdentifier = fingerprintTemplate.finger,
                                templateQualityScore = fingerprintTemplate.quality,
                                template = encoder.base64ToBytes(fingerprintTemplate.template),
                                format = fingerprintReference.format,
                            )
                        }
                    }
            )
        }

    private fun loadEnrolmentRecordCreationEvents(
        range: IntRange,
        callerPackageName: String,
        query: SubjectQuery,
    ): List<EnrolmentRecordCreationEvent> {
        val enrolmentRecordCreationEvents: MutableList<EnrolmentRecordCreationEvent> = mutableListOf()
        try {
            val caseId = attemptExtractingCaseId(query.metadata)
            if (caseId != null) {
                return loadEnrolmentRecordCreationEvents(caseId, callerPackageName, query)
            }

            context.contentResolver.query(
                getCaseMetadataUri(callerPackageName), null, null, null, null
            )?.use { caseMetadataCursor ->
                if (caseMetadataCursor.moveToPosition(range.first)) {
                    do {
                        caseMetadataCursor.getString(caseMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID))?.let { caseId ->
                            enrolmentRecordCreationEvents.addAll(loadEnrolmentRecordCreationEvents(caseId, callerPackageName, query))
                        }
                    } while (caseMetadataCursor.moveToNext() && caseMetadataCursor.position < range.last)
                }
            }
        } catch (e: Exception) {
            Simber.e("Error while querying CommCare", e)
        }

        return enrolmentRecordCreationEvents
    }

    private fun attemptExtractingCaseId(metadata: String?) = metadata
        ?.takeUnless { it.isEmpty() }
        ?.let {
            try {
                JsonHelper.fromJson<Map<String, Any>>(it)[ARG_CASE_ID] as? String
            } catch (_: JSONException) {
                null
            }
        }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FaceIdentity> = loadEnrolmentRecordCreationEvents(range, dataSource.callerPackageName(), query)
        .filter { erce -> erce.payload.biometricReferences.any { it is FaceReference } }
        .map {
            FaceIdentity(
                it.payload.subjectId,
                it.payload.biometricReferences.filterIsInstance<FaceReference>()
                    .flatMap { faceReference ->
                        faceReference.templates.map { faceTemplate ->
                            FaceSample(
                                template = encoder.base64ToBytes(faceTemplate.template),
                                format = faceReference.format,
                            )
                        }
                    }
            )
        }

    private fun loadEnrolmentRecordCreationEvents(
        caseId: String,
        callerPackageName: String,
        query: SubjectQuery,
    ): List<EnrolmentRecordCreationEvent> {
        //Access Case Data Listing for the caseId
        val caseDataUri = getCaseDataUri(callerPackageName).buildUpon().appendPath(caseId).build()

        return context.contentResolver.query(caseDataUri, null, null, null, null)?.use { caseDataCursor ->
            var subjectActions = getSubjectActionsValue(caseDataCursor)
            Simber.d(subjectActions)
            val coSyncEnrolmentRecordEvents = parseRecordEvents(subjectActions)

            coSyncEnrolmentRecordEvents?.events
                ?.filterIsInstance<EnrolmentRecordCreationEvent>()
                ?.filterNot { event ->
                    (query.subjectId != null && query.subjectId != event.payload.subjectId)
                        || (query.attendantId != null && query.attendantId != event.payload.attendantId.value)
                        || (query.moduleId != null && query.moduleId != event.payload.moduleId.value)
                }
        }.orEmpty()
    }

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
                type = object : TypeReference<CoSyncEnrolmentRecordEvents>() {}
            )
        } catch (e: Exception) {
            Simber.e("Error while parsing subjectActions", e)
            null
        }
    }

    private val coSyncSerializationModule = SimpleModule().apply {
        addSerializer(
            TokenizableString::class.java,
            TokenizationClassNameSerializer()
        )
        addDeserializer(
            TokenizableString::class.java,
            TokenizationClassNameDeserializer()
        )
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int {
        var count = 0
        context.contentResolver
            .query(getCaseMetadataUri(dataSource.callerPackageName()), null, null, null, null)
            ?.use { caseMetadataCursor -> count = caseMetadataCursor.count }

        return count
    }
}
