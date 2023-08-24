package com.simprints.feature.clientapi.models


internal object OdkConstants {

    const val PACKAGE_NAME = "com.simprints.simodkadapter"
    const val ACTION_ENROL = "$PACKAGE_NAME.REGISTER"
    const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
    const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
    const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    const val ACTION_ENROL_LAST_BIOMETRICS = "$PACKAGE_NAME.REGISTER_LAST_BIOMETRICS"


    private const val ODK_GUIDS_KEY = "odk-guids"
    private const val ODK_BIOMETRICS_COMPLETE_CHECK_KEY = "odk-biometrics-complete"
    private const val ODK_CONFIDENCES_KEY = "odk-confidences"
    private const val ODK_TIERS_KEY = "odk-tiers"
    private const val ODK_SESSION_ID = "odk-session-id"
    private const val ODK_EXIT_REASON = "odk-exit-reason"
    private const val ODK_EXIT_EXTRA = "odk-exit-extra"

    private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
    private const val ODK_REGISTER_BIOMETRICS_COMPLETE = "odk-register-biometrics-complete"

    private const val ODK_IDENTIFY_BIOMETRICS_COMPLETE = "odk-identify-biometrics-complete"
    private const val ODK_MATCH_CONFIDENCE_FLAGS_KEY = "odk-match-confidence-flags"
    private const val ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY =
        "odk-highest-match-confidence-flag"

    private const val ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE =
        "odk-confirm-identity-biometrics-complete"

    private const val ODK_VERIFY_BIOMETRICS_COMPLETE = "odk-verify-biometrics-complete"


    //For some reason, Survey CTO sends the callback field in the callout Intent.
    //Because SID doesn't expect these fields, the intent is marked as suspicious.
    //Added these fields as "acceptable", so a Suspicious event is not generated.
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
        ODK_VERIFY_BIOMETRICS_COMPLETE
    )
}

internal object CommCareConstants {
    const val PACKAGE_NAME = "com.simprints.commcare"
    const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
    const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
    const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
    const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    const val ACTION_ENROL_LAST_BIOMETRICS = "$PACKAGE_NAME.REGISTER_LAST_BIOMETRICS"
}

internal object LibSimprintsConstants {

    const val PACKAGE_NAME = "com.simprints.id"

    const val PROJECT_ID_LENGTH = 20
}
