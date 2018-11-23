package com.simprints.id.shared

import android.util.Base64
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.shared.models.TestCalloutCredentials


object DefaultTestConstants {
    const val DEFAULT_PROJECT_ID = "bWOFHInKA2YaQwrxZ7uJ"
    const val DEFAULT_MODULE_ID = "the_one_and_only_module"
    const val DEFAULT_USER_ID = "the_lone_user"
    const val DEFAULT_LEGACY_API_KEY = "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f"
    const val DEFAULT_PROJECT_SECRET = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    val DEFAULT_TEST_CALLOUT_CREDENTIALS = TestCalloutCredentials(
        projectId = DEFAULT_PROJECT_ID,
        moduleId = DEFAULT_MODULE_ID,
        userId = DEFAULT_USER_ID,
        legacyApiKey = DEFAULT_LEGACY_API_KEY)

    const val DEFAULT_REALM_KEY_STRING = "Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ=="
    val DEFAULT_REALM_KEY: ByteArray = Base64.decode(DEFAULT_REALM_KEY_STRING, Base64.NO_WRAP)

    val DEFAULT_LOCAL_DB_KEY = LocalDbKey(
        projectId = DEFAULT_PROJECT_ID,
        value = DEFAULT_REALM_KEY,
        legacyApiKey = DEFAULT_LEGACY_API_KEY)
}

