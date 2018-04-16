package com.simprints.id.data.secure

import com.simprints.id.data.db.ProjectIdProvider
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager: ProjectIdProvider {

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
    fun storeProjectIdWithLegacyProjectIdPair(projectId: String, legacyProjectId: String?)

    fun getHashedLegacyProjectIdForProjectIdOrEmpty(projectId: String): String
    fun getProjectIdForHashedLegacyProjectIdOrEmpty(hashedLegacyApiKey: String): String
}
