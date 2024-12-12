package com.simprints.feature.orchestrator.steps

/**
 * Constant step ID definitions for a quick way to identify steps in a flow.
 *
 * IDs are prefixed and spaced out by modality to avoid collisions.
 * (e.g. common steps are 101+, fingerprint - 301+, face steps 501+)
 */
internal object StepId {
    // Common step ids
    private const val STEP_BASE_CORE = 100
    const val SETUP = STEP_BASE_CORE + 1
    const val FETCH_GUID = STEP_BASE_CORE + 2
    const val CONSENT = STEP_BASE_CORE + 3
    const val ENROL_LAST_BIOMETRIC = STEP_BASE_CORE + 4
    const val CONFIRM_IDENTITY = STEP_BASE_CORE + 5
    const val VALIDATE_ID_POOL = STEP_BASE_CORE + 6
    const val SELECT_SUBJECT_AGE = STEP_BASE_CORE + 7

    // Face step ids
    private const val STEP_BASE_FINGERPRINT = 300
    const val FINGERPRINT_CAPTURE = STEP_BASE_FINGERPRINT + 2
    const val FINGERPRINT_MATCHER = STEP_BASE_FINGERPRINT + 3

    // Face step ids
    private const val STEP_BASE_FACE = 500
    const val FACE_CAPTURE = STEP_BASE_FACE + 2
    const val FACE_MATCHER = STEP_BASE_FACE + 3
}
