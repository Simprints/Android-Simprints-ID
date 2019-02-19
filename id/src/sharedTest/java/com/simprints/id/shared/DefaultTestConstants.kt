package com.simprints.id.shared

import android.util.Base64
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.shared.models.TestCalloutCredentials


object DefaultTestConstants {
    const val DEFAULT_PROJECT_ID = "TESTzbq8ZBOs1LLOOH6p"
    const val DEFAULT_MODULE_ID = "the_one_and_only_module"
    const val DEFAULT_USER_ID = "the_lone_user"
    const val DEFAULT_LEGACY_API_KEY = "b011033e-85c4-4172-82ae-2d67ebdcb053"
    const val DEFAULT_PROJECT_SECRET = "AD4oXi/Cce8EwNTK7VjXHzx6tFqyvtJ/jZYGy/0EbuHIGAVM6jYHCWg67Pr53PeDR09aUWKk3yEgNdt3Xiqp9w=="

    val DEFAULT_TEST_CALLOUT_CREDENTIALS = TestCalloutCredentials(
        projectId = DEFAULT_PROJECT_ID,
        moduleId = DEFAULT_MODULE_ID,
        userId = DEFAULT_USER_ID,
        legacyApiKey = DEFAULT_LEGACY_API_KEY)

    private const val DEFAULT_REALM_KEY_STRING = "Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ=="
    val DEFAULT_REALM_KEY: ByteArray = Base64.decode(DEFAULT_REALM_KEY_STRING, Base64.NO_WRAP)

    val DEFAULT_LOCAL_DB_KEY = LocalDbKey(
        projectId = DEFAULT_PROJECT_ID,
        value = DEFAULT_REALM_KEY,
        legacyApiKey = DEFAULT_LEGACY_API_KEY)
}

