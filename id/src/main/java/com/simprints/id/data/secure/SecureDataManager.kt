package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager {

    var encryptedProjectSecret: String
    var projectId: String
    var prefs: ImprovedSharedPreferences
    fun getEncryptedProjectSecretOrEmpty(): String
    fun getProjectIdOrEmpty(): String
    fun areProjectCredentialsMissing(): Boolean

    /*TODO: Legacy stuff to refactor */
    fun getApiKeyOr(default: String): String
    var apiKey: String
}
