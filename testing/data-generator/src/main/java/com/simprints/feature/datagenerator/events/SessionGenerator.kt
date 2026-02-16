package com.simprints.feature.datagenerator.events

import javax.inject.Inject

class SessionGenerator @Inject constructor(
    private val sqlEventTemplateLoader: SqlEventTemplateLoader,
) {
    /**
     * Corresponds to `Enrollment-roc-3.json`.
     * A session where a user is identified and then a new enrolment is created.
     */
    fun generateEnrolLastBioRoc3(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = ENROL_LAST_BIO_ROC3_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    /**
     * Corresponds to `Identification-roc-3.json`.
     * A standard identification session.
     */
    fun generateIdentificationRoc3(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = IDENTIFICATION_ROC3_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    fun generateIdentificationRoc3ExternalCredential(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = IDENTIFICATION_EXTERNAL_CREDENTIAL_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    /**
     * Corresponds to `Verification-roc-3.json`.
     * A standard verification session against a known GUID.
     */
    fun generateVerificationRoc3(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = VERIFICATION_ROC3_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    /**
     * Corresponds to `Confirmation-roc-3.json`.
     * An identification followed by a user selecting a GUID for confirmation.
     */
    fun generateConfirmationRoc3(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = CONFIRMATION_ROC3_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    /**
     * Corresponds to `Enrolment-SimFace.json`.
     * A direct face enrolment using Simprints' face algorithm.
     */
    fun generateEnrolmentSimFace(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = ENROLMENT_SIMFACE_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    fun generateEnrolmentSimFaceExternalCredential(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = ENROLMENT_EXTERNAL_CREDENTIAL_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    /**
     * Corresponds to `Enrolment-ISO.json`.
     * A fingerprint enrolment session using a vero scanner ISO templates.
     */
    fun generateEnrolmentIso(
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = loadEventsSql(
        eventNames = ENROLMENT_ISO_EVENTS,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        scopeId = scopeId,
    )

    private fun loadEventsSql(
        eventNames: List<String>,
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ) = eventNames.map { eventName ->
        sqlEventTemplateLoader.getSql(
            eventName = eventName,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            scopeId = scopeId,
        )
    }

    fun clearCache() {
        sqlEventTemplateLoader.clearCache()
    }

    companion object {
        val ENROL_LAST_BIO_ROC3_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_IDENTIFICATION",
            "AUTHORIZATION",
            "CONSENT",
            "FACE_FALLBACK_CAPTURE",
            "LICENSE_CHECK",
            "FACE_CAPTURE",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "ONE_TO_MANY_MATCH",
            "CALLBACK_IDENTIFICATION",
            "CALLOUT_ENROLMENT",
            "AUTHORIZATION",
            "ENROLMENT",
            "CALLBACK_ENROLMENT",
            "COMPLETION_CHECK",
        )

        val IDENTIFICATION_ROC3_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_IDENTIFICATION",
            "AUTHORIZATION",
            "CONSENT",
            "FACE_FALLBACK_CAPTURE",
            "LICENSE_CHECK",
            "FACE_CAPTURE",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "ONE_TO_MANY_MATCH",
            "CALLBACK_IDENTIFICATION",
            "COMPLETION_CHECK",
        )

        val VERIFICATION_ROC3_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_VERIFICATION",
            "AUTHORIZATION",
            "CANDIDATE_READ",
            "CONSENT",
            "FACE_FALLBACK_CAPTURE",
            "LICENSE_CHECK",
            "FACE_CAPTURE",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "ONE_TO_ONE_MATCH",
            "CALLBACK_VERIFICATION",
            "COMPLETION_CHECK",
        )

        val CONFIRMATION_ROC3_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_IDENTIFICATION",
            "AUTHORIZATION",
            "CONSENT",
            "FACE_FALLBACK_CAPTURE",
            "LICENSE_CHECK",
            "FACE_CAPTURE",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "ONE_TO_MANY_MATCH",
            "CALLBACK_IDENTIFICATION",
            "COMPLETION_CHECK",
            "CALLOUT_CONFIRMATION",
            "AUTHORIZATION",
            "GUID_SELECTION",
            "CALLBACK_CONFIRMATION",
            "COMPLETION_CHECK",
        )

        val ENROLMENT_SIMFACE_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_ENROLMENT",
            "AUTHORIZATION",
            "CONSENT",
            "FACE_FALLBACK_CAPTURE",
            "FACE_CAPTURE",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "ENROLMENT",
            "CALLBACK_ENROLMENT",
            "COMPLETION_CHECK",
        )

        val ENROLMENT_EXTERNAL_CREDENTIAL_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_ENROLMENT",
            "AUTHENTICATION",
            "CONSENT",
            "FACE_ONBOARDING_COMPLETE",
            "LICENSE_CHECK",
            "FACE_FALLBACK_CAPTURE",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "EXTERNAL_CREDENTIAL_SELECTION",
            "EXTERNAL_CREDENTIAL_CAPTURE",
            "EXTERNAL_CREDENTIAL_CAPTURE_VALUE",
            "EXTERNAL_CREDENTIAL_CONFIRMATION",
            "EXTERNAL_CREDENTIAL_SEARCH",
            "ENROLMENT",
            "CALLBACK_ENROLMENT",
            "COMPLETION_CHECK",
        )

        val IDENTIFICATION_EXTERNAL_CREDENTIAL_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_IDENTIFICATION",
            "AUTHORIZATION",
            "CONSENT",
            "FACE_FALLBACK_CAPTURE",
            "LICENSE_CHECK",
            "FACE_CAPTURE_BIOMETRICS",
            "FACE_CAPTURE",
            "FACE_CAPTURE_CONFIRMATION",
            "BIOMETRIC_REFERENCE_CREATION",
            "EXTERNAL_CREDENTIAL_SELECTION",
            "EXTERNAL_CREDENTIAL_CAPTURE",
            "EXTERNAL_CREDENTIAL_CAPTURE_VALUE",
            "EXTERNAL_CREDENTIAL_CONFIRMATION",
            "EXTERNAL_CREDENTIAL_SEARCH",
            "ONE_TO_ONE_MATCH",
            "CALLBACK_IDENTIFICATION",
            "COMPLETION_CHECK",
        )

        val ENROLMENT_ISO_EVENTS = listOf(
            "INTENT_PARSING",
            "CONNECTIVITY_SNAPSHOT",
            "CALLOUT_ENROLMENT",
            "AUTHORIZATION",
            "CONSENT",
            "SCANNER_CONNECTION",
            "VERO_2_INFO_SNAPSHOT",
            "FINGERPRINT_CAPTURE",
            "FINGERPRINT_CAPTURE_BIOMETRICS",
            "FINGERPRINT_CAPTURE",
            "FINGERPRINT_CAPTURE_BIOMETRICS",
            "BIOMETRIC_REFERENCE_CREATION",
            "ENROLMENT",
            "CALLBACK_ENROLMENT",
            "COMPLETION_CHECK",
        )
    }
}
