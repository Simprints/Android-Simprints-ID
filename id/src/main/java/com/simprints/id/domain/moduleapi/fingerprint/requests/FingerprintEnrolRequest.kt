package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintEnrolRequest(val projectId: String,
                                   val userId: String,
                                   val moduleId: String,
                                   val metadata: String,
                                   val language: String,
                                   val fingerStatus: Map<FingerprintFingerIdentifier, Boolean>,
                                   val logoExists: Boolean,
                                   val programName: String,
                                   val organizationName: String) : FingerprintRequest() {

    companion object {
        private const val FIELD_PROJECT_ID = "projectId"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_MODULE_ID = "moduleId"
        private const val FIELD_METADATA = "metadata"
        private const val FIELD_LANGUAGE = "language"
        private const val FIELD_FINGER_STATUS = "fingerStatus"
        private const val FIELD_LOGO_EXISTS = "logoExists"
        private const val FIELD_PROGRAMME_NAME = "programName"
        private const val FIELD_ORGANISATION_NAME = "organizationName"

        @Suppress("UNCHECKED_CAST")
        fun tryParse(jsonReader: JsonReader): FingerprintEnrolRequest? {
            var request: FingerprintEnrolRequest? = null


            with(jsonReader) {
                beginObject()

                while (hasNext()) {
                    val elementName = nextName()
                    if (FingerprintEnrolRequest::class.java.fields.none { it.name == elementName }) {
                        break
                    } else {
                        var projectId = ""
                        var userId = ""
                        var moduleId = ""
                        var metadata = ""
                        var language = ""
                        var fingerStatus = mapOf<FingerprintFingerIdentifier, Boolean>()
                        var logoExists = false
                        var programmeName = ""
                        var organisationName = ""

                        when (elementName) {
                            FIELD_PROJECT_ID -> projectId = nextString()
                            FIELD_USER_ID -> userId = nextString()
                            FIELD_MODULE_ID -> moduleId = nextString()
                            FIELD_METADATA -> metadata = nextString()
                            FIELD_LANGUAGE -> language = nextString()
                            FIELD_FINGER_STATUS -> fingerStatus = JsonHelper.gson.fromJson(nextString(), Map::class.java) as Map<FingerprintFingerIdentifier, Boolean>
                            FIELD_LOGO_EXISTS -> logoExists = nextBoolean()
                            FIELD_PROGRAMME_NAME -> programmeName = nextString()
                            FIELD_ORGANISATION_NAME -> organisationName = nextString()
                        }

                        val fieldsAreValid = projectId != ""
                            && userId != ""
                            && moduleId != ""
                            && metadata != ""
                            && language != ""
                            && fingerStatus.isNotEmpty()
                            && programmeName != ""
                            && organisationName != ""

                        if (fieldsAreValid) {
                            request = FingerprintEnrolRequest(
                                projectId,
                                userId,
                                moduleId,
                                metadata,
                                language,
                                fingerStatus,
                                logoExists,
                                programmeName,
                                organisationName
                            )
                            break
                        }
                    }
                }

                endObject()
            }

            return request
        }
    }

}
