package com.simprints.infra.enrolment.records.store.commcare

import android.util.JsonReader
import android.util.JsonToken
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.enrolment.records.store.commcare.model.BiometricReferenceWithId
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.BiometricReferenceType
import com.simprints.infra.events.event.domain.models.subject.BiometricReferenceType.FACE_REFERENCE
import com.simprints.infra.events.event.domain.models.subject.BiometricReferenceType.FINGERPRINT_REFERENCE
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import java.io.StringReader

class JsonEventParser {
    fun getRecordEvents(json: String): List<BiometricReferenceWithId> {
        val referencesWithId: MutableList<BiometricReferenceWithId> = mutableListOf()
        val reader = JsonReader(StringReader(json))

        reader.use {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "events" -> referencesWithId.addAll(readEvents(reader))
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
        }

        return referencesWithId
    }

    private fun readEvents(reader: JsonReader): List<BiometricReferenceWithId> {
        val events = mutableListOf<BiometricReferenceWithId>()
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "type" -> {
                        if (reader.nextString() != ENROLMENT_RECORD_TYPE) {
                            reader.skipValue()
                            break
                        }
                    }

                    "payload" -> events.add(readPayload(reader))
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }

        reader.endArray()
        return events
    }

    private fun readPayload(reader: JsonReader): BiometricReferenceWithId {
        reader.beginObject()

        var subjectId = ""
        var projectId = ""
        var moduleId = ""
        var attendantId = ""
        val biometricReferences = mutableListOf<BiometricReference>()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "projectId" -> projectId = reader.nextString()
                "subjectId" -> subjectId = reader.nextString()
                "moduleId" -> moduleId = readFlexibleString(reader)
                "attendantId" -> attendantId = readFlexibleString(reader)
                "biometricReferences" -> biometricReferences.addAll(readBiometricRefs(reader))
                else -> reader.skipValue()
            }
        }

        reader.endObject()
        return BiometricReferenceWithId(
            subjectId = subjectId,
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            biometricReferences = biometricReferences
        )
    }

    /**
     * Reads string that might be a deserialized tokenized string or a plain string
     * Example:
     *      "attendantId":{
     *        "value":"cd6fbed82c07"
     *      }
     *
     *      or
     *
     *      "attendantId": "cd6fbed82c07"
     *
     */
    private fun readFlexibleString(reader: JsonReader): String {
        return if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            readTokenizedString(reader)
        } else {
            reader.nextString()
        }
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

    private fun readBiometricRefs(reader: JsonReader): List<BiometricReference> {
        reader.beginArray()
        val biometricReference = mutableListOf<BiometricReference>()

        while (reader.hasNext()) {
            reader.beginObject()
            var id = ""
            var format = ""
            var type: BiometricReferenceType? = null
            val jsonRawTemplates = mutableListOf<JsonTemplate>()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextString()
                    "templates" -> jsonRawTemplates.addAll(readFingerTemplates(reader))
                    "format" -> format = reader.nextString()
                    "type" -> when (reader.nextString()) {
                        FINGERPRINT_REFERENCE_TYPE -> type = FINGERPRINT_REFERENCE
                        FACE_REFERENCE_TYPE -> type = FACE_REFERENCE
                        else -> reader.skipValue()
                    }

                    else -> reader.skipValue()
                }
            }
            when (type) {
                FACE_REFERENCE -> biometricReference.add(
                    FaceReference(
                        id = id,
                        templates = jsonRawTemplates.map {
                            FaceTemplate(template = it.template)
                        },
                        format = format
                    )
                )

                FINGERPRINT_REFERENCE -> biometricReference.add(
                    FingerprintReference(
                        id = id,
                        templates = jsonRawTemplates.mapNotNull {
                            if(it.quality != null && it.finger != null) {
                                FingerprintTemplate(
                                    quality = it.quality,
                                    template = it.template,
                                    finger = IFingerIdentifier.valueOf(it.finger)
                                )
                            } else null
                        },
                        format = format
                    )
                )

                null -> {
                    /*do nothing*/
                }
            }
            reader.endObject()
        }

        reader.endArray()
        return biometricReference
    }

    private fun readFingerTemplates(reader: JsonReader): List<JsonTemplate> {
        reader.beginArray()
        val templates = mutableListOf<JsonTemplate>()
        while (reader.hasNext()) {
            reader.beginObject()
            var template = ""
            var quality: Int? = null
            var finger: String? = null
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "quality" -> quality = reader.nextInt()
                    "template" -> template = reader.nextString()
                    "finger" -> finger = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            templates.add(
                JsonTemplate(
                    quality = quality,
                    template = template,
                    finger = finger
                )
            )
            reader.endObject()
        }
        reader.endArray()
        return templates
    }

    private data class JsonTemplate(
        val quality: Int?,
        val template: String,
        val finger: String?,
    )

    companion object {
        private const val FINGERPRINT_REFERENCE_TYPE = "FINGERPRINT_REFERENCE"
        private const val FACE_REFERENCE_TYPE = "FACE_REFERENCE"
        private const val ENROLMENT_RECORD_TYPE = "EnrolmentRecordCreation"
    }
}
