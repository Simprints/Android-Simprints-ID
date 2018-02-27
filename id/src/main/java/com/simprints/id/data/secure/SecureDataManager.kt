package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager {

    var encryptedProjectSecret: String
    var signedInProjectId: String
    var signedInUserId: String
    var prefs: ImprovedSharedPreferences
    fun getEncryptedProjectSecretOrEmpty(): String
    fun getSignedInLegacyApiKeyOrEmpty(): String

    fun getSignedInProjectIdOrEmpty(): String
    fun getSignedInUserIdOrEmpty(): String
    fun isProjectIdSignedIn(possibleProjectId: String): Boolean
    fun cleanCredentials()
    fun storeProjectIdWithLegacyApiKeyPair(projectId: String, legacyApiKey: String?)
    fun projectIdForLegacyApiKeyOrEmpty(legacyApiKey: String): String
    fun legacyApiKeyForProjectIdOrEmpty(projectId: String): String
}
