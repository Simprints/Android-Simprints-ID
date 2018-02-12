package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager {

    var encryptedProjectSecret: String
    var signedInProjectId: String
    var prefs: ImprovedSharedPreferences
    fun getEncryptedProjectSecretOrEmpty(): String
    fun getSignedInProjectIdOrEmpty(): String
    fun isProjectIdSignedIn(projectId: String): Boolean
    fun cleanCredentials()
    fun storeProjectIdWithLegacyApiKeyPair(possibleProjectId: String, possibleLegacyApiKey: String?)
    fun projectIdForLegacyApiKeyOrEmpty(legacyApiKey: String): String
}
