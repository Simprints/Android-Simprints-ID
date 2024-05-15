package com.simprints.infra.enrolment.records.store.commcare

import android.content.Context
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
import javax.inject.Inject

internal class CommCareIdentityDataSource @Inject constructor(
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
    @ApplicationContext private val context: Context,
) : IdentityDataSource {

    companion object {
        val CASE_METADATA_URI: Uri = Uri.parse("content://org.commcare.dalvik.case/casedb/case")
        val CASE_DATA_URI: Uri = Uri.parse("content://org.commcare.dalvik.case/casedb/data")
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
    ): List<FingerprintIdentity> = loadEnrolmentRecordCreationEvents(range)
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

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
    ): List<FaceIdentity> = loadEnrolmentRecordCreationEvents(range)
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

    private fun loadEnrolmentRecordCreationEvents(range: IntRange): List<EnrolmentRecordCreationEvent> {
        val enrolmentRecordCreationEvents: MutableList<EnrolmentRecordCreationEvent> = mutableListOf()

        try {
            context.contentResolver.query(CASE_METADATA_URI, null, null, null, null)?.use { caseMetadataCursor ->
                if (caseMetadataCursor.moveToPosition(range.first)) {
                    do {
                        caseMetadataCursor.getString(caseMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID))?.let { caseId ->
                            enrolmentRecordCreationEvents.addAll(loadEnrolmentRecordCreationEvents(caseId))
                        }
                    } while (caseMetadataCursor.moveToNext() && caseMetadataCursor.position < range.last)
                }
            }
        } catch (e: Exception) {
            Simber.e("Error while querying CommCare", e)
        }

        return enrolmentRecordCreationEvents
    }

    private fun loadEnrolmentRecordCreationEvents(caseId: String): List<EnrolmentRecordCreationEvent> {
        val caseEnrolmentRecordCreationEvents: MutableList<EnrolmentRecordCreationEvent> = mutableListOf()

        //Access Case Data Listing for the caseId
        val caseDataUri = CASE_DATA_URI.buildUpon().appendPath(caseId).build()
        context.contentResolver.query(caseDataUri, null, null, null, null)?.use { caseDataCursor ->
            var subjectActions = ""
            while (caseDataCursor.moveToNext()) {
                val key = caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID))
                if (key == SIMPRINTS_COSYNC_SUBJECT_ACTIONS) {
                    subjectActions = caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_VALUE))
                    break
                }
            }

            val coSyncEnrolmentRecordEvents =  subjectActions.takeIf(String::isNotEmpty)?.let {
                try {
                    val coSyncSerializationModule = SimpleModule().apply {
                        addSerializer(
                            TokenizableString::class.java,
                            TokenizationClassNameSerializer()
                        )
                        addDeserializer(
                            TokenizableString::class.java,
                            TokenizationClassNameDeserializer()
                        )
                    }
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
            coSyncEnrolmentRecordEvents?.events?.filterIsInstance<EnrolmentRecordCreationEvent>()
                ?.let { events ->
                    caseEnrolmentRecordCreationEvents.addAll(events)
                }

            Simber.d(subjectActions)
        }

        return caseEnrolmentRecordCreationEvents
    }

    override suspend fun count(query: SubjectQuery): Int {
        var count = 0
        context.contentResolver.query(CASE_METADATA_URI, null, null, null, null)?.use { caseMetadataCursor ->
            count = caseMetadataCursor.count
        }

        return count
    }
}
