package com.simprints.id.domain

import android.app.Activity

class Constants {

    companion object {

        const val GLOBAL_ID = "GLOBAL-2b14bf72-b68a-4c24-acaf-66d5e1fcc4bc"

        // Intents
        const val SIMPRINTS_REGISTER_INTENT = "com.simprints.id.REGISTER"
        const val SIMPRINTS_IDENTIFY_INTENT = "com.simprints.id.IDENTIFY"
        const val SIMPRINTS_UPDATE_INTENT = "com.simprints.id.UPDATE"
        const val SIMPRINTS_VERIFY_INTENT = "com.simprints.id.VERIFY"
        const val SIMPRINTS_SELECT_GUID_INTENT = "com.simprints.id.CONFIRM_IDENTITY"

        // Mandatory extras
        const val SIMPRINTS_PROJECT_ID = "projectId"
        const val SIMPRINTS_USER_ID = "userId"
        const val SIMPRINTS_MODULE_ID = "moduleId"

        // Mandatory for SIMPRINTS_UPDATE_INTENT
        const val SIMPRINTS_UPDATE_GUID = "updateGuid"

        // Mandatory for SIMPRINTS_VERIFY_INTENT
        const val SIMPRINTS_VERIFY_GUID = "verifyGuid"

        // Mandatory for SIMPRINTS_SELECT_GUID_INTENT
        const val SIMPRINTS_PACKAGE_NAME = "com.simprints.id"
        const val SIMPRINTS_SELECTED_GUID = "selectedGuid"
        const val SIMPRINTS_SESSION_ID = "sessionId"

        // Optional extras
        const val SIMPRINTS_CALLING_PACKAGE = "packageName"
        const val SIMPRINTS_METADATA = "metadata"

        // Custom callout parameters for particular integrations: Don't include if not needed
        const val SIMPRINTS_RESULT_FORMAT = "resultFormat"
        const val SIMPRINTS_ODK_RESULT_FORMAT_V01 = "ODKv01"
        const val SIMPRINTS_ODK_RESULT_FORMAT_V01_SEPARATOR = " "

        // Result codes
        const val SIMPRINTS_OK = Activity.RESULT_OK
        const val SIMPRINTS_CANCELLED = Activity.RESULT_CANCELED
        const val SIMPRINTS_MISSING_USER_ID = Activity.RESULT_FIRST_USER + 2
        const val SIMPRINTS_MISSING_MODULE_ID = Activity.RESULT_FIRST_USER + 4
        const val SIMPRINTS_INVALID_INTENT_ACTION = Activity.RESULT_FIRST_USER + 6
        const val SIMPRINTS_INVALID_UPDATE_GUID = Activity.RESULT_FIRST_USER + 7
        const val SIMPRINTS_MISSING_UPDATE_GUID = Activity.RESULT_FIRST_USER + 8
        const val SIMPRINTS_MISSING_VERIFY_GUID = Activity.RESULT_FIRST_USER + 9
        const val SIMPRINTS_INVALID_METADATA = Activity.RESULT_FIRST_USER + 10
        const val SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE = Activity.RESULT_FIRST_USER + 11
        const val SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE = Activity.RESULT_FIRST_USER + 12
        const val SIMPRINTS_INVALID_VERIFY_GUID = Activity.RESULT_FIRST_USER + 13
        const val SIMPRINTS_INVALID_RESULT_FORMAT = Activity.RESULT_FIRST_USER + 14
        const val SIMPRINTS_INVALID_MODULE_ID = Activity.RESULT_FIRST_USER + 15
        const val SIMPRINTS_INVALID_USER_ID = Activity.RESULT_FIRST_USER + 16
        const val SIMPRINTS_INVALID_CALLING_PACKAGE = Activity.RESULT_FIRST_USER + 17
        const val SIMPRINTS_MISSING_PROJECT_ID = Activity.RESULT_FIRST_USER + 18
        const val SIMPRINTS_INVALID_PROJECT_ID = Activity.RESULT_FIRST_USER + 19
        const val SIMPRINTS_DIFFERENT_PROJECT_ID = Activity.RESULT_FIRST_USER + 20
        const val SIMPRINTS_DIFFERENT_USER_ID = Activity.RESULT_FIRST_USER + 21

        // Result extras
        const val SIMPRINTS_REGISTRATION = "registration"
        const val SIMPRINTS_IDENTIFICATIONS = "identification"
        const val SIMPRINTS_VERIFICATION = "verification"
        const val SIMPRINTS_REFUSAL_FORM = "refusalForm"

        // Deprecated extras

        @Deprecated("use {@link #SIMPRINTS_PROJECT_ID} instead. ")
        const val SIMPRINTS_API_KEY = "apiKey"

        // Deprecated result codes

        @Deprecated("use {@link #SIMPRINTS_MISSING_PROJECT_ID} instead. ")
        const val SIMPRINTS_MISSING_API_KEY = Activity.RESULT_FIRST_USER

        @Deprecated("use {@link #SIMPRINTS_INVALID_PROJECT_ID} instead. ")
        const val SIMPRINTS_INVALID_API_KEY = Activity.RESULT_FIRST_USER + 1
    }
}
