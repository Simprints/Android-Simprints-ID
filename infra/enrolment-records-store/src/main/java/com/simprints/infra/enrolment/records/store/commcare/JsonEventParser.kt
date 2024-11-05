package com.simprints.infra.enrolment.records.store.commcare

import android.util.JsonReader
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import java.io.StringReader

class JsonEventParser {

    fun getRecordEvents(json: String): CoSyncEnrolmentRecordEvents {
        val events: MutableList<EnrolmentRecordCreationEvent> = mutableListOf()
        val reader = JsonReader(StringReader(json))

        reader.use {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "events" -> events.addAll(readEvents(reader))
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
        }

        return CoSyncEnrolmentRecordEvents(
            events = events
        )
    }

    private fun readEvents(reader: JsonReader): List<EnrolmentRecordCreationEvent> {
        val events = mutableListOf<EnrolmentRecordCreationEvent>()
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "payload" -> events.add(readPayload(reader))
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }

        reader.endArray()
        return events
    }

    private fun readPayload(reader: JsonReader): EnrolmentRecordCreationEvent {
        reader.beginObject()

        var subjectId = ""
        var projectId = ""
        var moduleId = ""
        var attendantId = ""
        val biometricReferences = mutableListOf<FingerprintReference>()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "projectId" -> projectId = reader.nextString()
                "subjectId" -> subjectId = reader.nextString()
                "moduleId" -> moduleId = readTokenizedString(reader)
                "attendantId" -> attendantId = readTokenizedString(reader)
                "biometricReferences" -> biometricReferences.addAll(readBiometricRefs(reader))
                else -> reader.skipValue()
            }
        }

        reader.endObject()
        return EnrolmentRecordCreationEvent(
            subjectId = subjectId,
            projectId = projectId,
            moduleId = TokenizableString.Raw(moduleId),
            attendantId = TokenizableString.Raw(attendantId),
            biometricReferences = biometricReferences
        )
    }

    private fun readTokenizedString(reader: JsonReader): String {
        var value = ""
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "value" -> value = reader.nextString()
            }
        }
        reader.endObject()
        return value
    }

    private fun readBiometricRefs(reader: JsonReader): List<FingerprintReference> {
        reader.beginArray()
        val fingerprintReferences = mutableListOf<FingerprintReference>()

        while (reader.hasNext()) {
            reader.beginObject()
            var id = ""
            var format = ""
            val templates = mutableListOf<FingerprintTemplate>()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextString()
                    "templates" -> templates.addAll(readTemplates(reader))
                    "format" -> format = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            fingerprintReferences.add(
                FingerprintReference(
                    id = id,
                    templates = templates,
                    format = format
                )
            )
            reader.endObject()
        }

        reader.endArray()
        return fingerprintReferences
    }

    private fun readTemplates(reader: JsonReader): List<FingerprintTemplate> {
        reader.beginArray()
        val templates = mutableListOf<FingerprintTemplate>()
        while (reader.hasNext()) {
            reader.beginObject()
            var template = ""
            var quality = 0
            var finger = ""
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "quality" -> quality = reader.nextInt()
                    "template" -> template = reader.nextString()
                    "finger" -> finger = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            templates.add(
                FingerprintTemplate(
                    quality,
                    template,
                    IFingerIdentifier.valueOf(finger)
                )
            )
            reader.endObject()
        }
        reader.endArray()
        return templates
    }
}
