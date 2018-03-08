package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager {

    var encryptedProjectSecret: String
    var signedInProjectId: String
    var signedInUserId: String
    var prefs: ImprovedSharedPreferences
    fun getEncryptedProjectSecretOrEmpty(): String
    fun getSignedInHashedLegacyApiKeyOrEmpty(): String

    fun getSignedInProjectIdOrEmpty(): String
    fun getSignedInUserIdOrEmpty(): String
    fun isProjectIdSignedIn(possibleProjectId: String): Boolean
    fun cleanCredentials()
    fun storeProjectIdWithLegacyApiKeyPair(projectId: String, legacyApiKey: String?)

    fun getHashedLegacyApiKeyForProjectIdOrEmpty(projectId: String): String
    fun getProjectIdForHashedLegacyApiKeyOrEmpty(hashedLegacyApiKey: String): String
}
