package com.simprints.id.shared.models

import com.simprints.id.shared.DefaultTestConstants.DEFAULT_LEGACY_API_KEY
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_USER_ID

data class TestCalloutCredentials(val projectId: String = DEFAULT_PROJECT_ID,
                                  val moduleId: String = DEFAULT_MODULE_ID,
                                  val userId: String = DEFAULT_USER_ID,
                                  val legacyApiKey: String = DEFAULT_LEGACY_API_KEY) {

    fun toLegacy() =
        TestCalloutCredentials("", moduleId, userId, legacyApiKey)
}
