package com.simprints.id.tools


data class CalloutCredentials(val projectId: String, val moduleId: String, val userId: String, val legacyApiKey: String = "") {

    fun toLegacy() =
        CalloutCredentials("", moduleId, userId, legacyApiKey)
}
