package com.simprints.core.login

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences


interface LoginInfoManager {

    // Cached claims in the auth token. We used them to check whether the user is signed or not
    // in without reading the token from Firebase (async operation)
    var projectIdTokenClaim: String?
    var userIdTokenClaim: String?

    var encryptedProjectSecret: String
    var signedInProjectId: String
    var signedInUserId: String
    var prefs: ImprovedSharedPreferences
    fun getEncryptedProjectSecretOrEmpty(): String

    fun getSignedInProjectIdOrEmpty(): String

    fun getSignedInUserIdOrEmpty(): String
    fun isProjectIdSignedIn(possibleProjectId: String): Boolean
    fun cleanCredentials()
    fun clearCachedTokenClaims()
    fun storeCredentials(projectId: String, userId: String)
}
