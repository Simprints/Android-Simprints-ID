package com.simprints.infra.logging

object LoggingConstants {
    /**
     * These keys have been separate into two groups of constants because of legacy reasons.
     * They used to be captured in two separate managers, and in order not to break the backwards
     * compatibility of Analytics and Crashlytics we have kept the original keys. These can be
     * merged into a single set a keys on the next Firebase overhaul.
     */
    object CrashReportingCustomKeys {
        const val USER_ID = "User_ID"
        const val PROJECT_ID = "Project_ID"
        const val MODULE_IDS = "Module_IDs"
        const val DEVICE_ID = "Device_ID"
        const val SUBJECTS_DOWN_SYNC_TRIGGERS = "Down_sync_triggers"
        const val SESSION_ID = "Session_ID"
    }

    object AnalyticsUserProperties {
        const val USER_ID = "user_id"
        const val PROJECT_ID = "project_id"
        const val MODULE_ID = "module_id"
        const val DEVICE_ID = "device_id"

        const val MAC_ADDRESS = "mac_address"
        const val SCANNER_ID = "scanner_id"
    }

    enum class CrashReportTag {
        LOGIN,
        LOGOUT,
        SYNC,
        SESSION,
        FINGER_CAPTURE,
        FACE_CAPTURE,
        FINGER_MATCHING,
        FACE_MATCHING,
        LICENSE,
        SETTINGS,
        ALERT,
        REALM_DB,
        DB_CORRUPTION,
        ENROLMENT,
        APP_SCOPE_ERROR,
        ORCHESTRATION,
        MIGRATION,
    }
}
