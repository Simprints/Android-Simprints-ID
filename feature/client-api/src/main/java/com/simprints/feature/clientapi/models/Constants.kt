package com.simprints.feature.clientapi.models

object ClientApiConstants {
    const val CALLER_PACKAGE_NAME = "callerPackageName"
}

internal object OdkConstants {
    const val PACKAGE_NAME = "com.simprints.simodkadapter"

    const val ODK_GUIDS_KEY = "odk-guids"
    const val ODK_BIOMETRICS_COMPLETE_CHECK_KEY = "odk-biometrics-complete"
    const val ODK_CONFIDENCES_KEY = "odk-confidences"
    const val ODK_TIERS_KEY = "odk-tiers"
    const val ODK_VERIFICATION_SUCCESS_KEY = "odk-verification-success"
    const val ODK_SESSION_ID = "odk-session-id"
    const val ODK_EXIT_REASON = "odk-exit-reason"
    const val ODK_EXIT_EXTRA = "odk-exit-extra"

    const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
    const val ODK_REGISTER_BIOMETRICS_COMPLETE = "odk-register-biometrics-complete"

    const val ODK_IDENTIFY_BIOMETRICS_COMPLETE = "odk-identify-biometrics-complete"
    const val ODK_MATCH_CONFIDENCE_FLAGS_KEY = "odk-match-confidence-flags"
    const val ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY = "odk-highest-match-confidence-flag"

    const val ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE = "odk-confirm-identity-biometrics-complete"

    const val ODK_VERIFY_BIOMETRICS_COMPLETE = "odk-verify-biometrics-complete"

    // For some reason, Survey CTO sends the callback field in the callout Intent.
    // Because SID doesn't expect these fields, the intent is marked as suspicious.
    // Added these fields as "acceptable", so a Suspicious event is not generated.
    val acceptableExtras = listOf(
        ODK_REGISTRATION_ID_KEY,
        ODK_GUIDS_KEY,
        ODK_BIOMETRICS_COMPLETE_CHECK_KEY,
        ODK_CONFIDENCES_KEY,
        ODK_TIERS_KEY,
        ODK_SESSION_ID,
        ODK_EXIT_REASON,
        ODK_EXIT_EXTRA,
        ODK_REGISTRATION_ID_KEY,
        ODK_REGISTER_BIOMETRICS_COMPLETE,
        ODK_IDENTIFY_BIOMETRICS_COMPLETE,
        ODK_MATCH_CONFIDENCE_FLAGS_KEY,
        ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY,
        ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE,
        ODK_VERIFY_BIOMETRICS_COMPLETE,
    )
}

internal object CommCareConstants {
    const val PACKAGE_NAME = "com.simprints.commcare"

    const val BIOMETRICS_COMPLETE_CHECK_KEY = "biometricsComplete"
    const val REGISTRATION_GUID_KEY = "guid"
    const val VERIFICATION_CONFIDENCE_KEY = "confidence"
    const val VERIFICATION_TIER_KEY = "tier"
    const val VERIFICATION_GUID_KEY = "guid"
    const val VERIFICATION_SUCCESS_KEY = "verificationSuccess"
    const val EXIT_REASON = "exitReason"
    const val EXIT_EXTRA = "exitExtra"
    const val SIMPRINTS_SESSION_ID = "sessionId"

    const val COMMCARE_BUNDLE_KEY = "odk_intent_bundle"
    const val COMMCARE_DATA_KEY = "odk_intent_data"
}

internal object LibSimprintsConstants {
    const val PACKAGE_NAME = "com.simprints.id"
}
