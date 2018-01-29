package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager {

    var projectSecret: String
    var projectId: String
    var prefs: ImprovedSharedPreferences
    fun getProjectSecretOrEmpty(): String
    fun getProjectIdOrEmpty(): String
    fun areProjectCredentialsStore(): Boolean

    /*TODO: Legacy stuff to refactor */
    fun getApiKeyOr(default: String): String
    var apiKey: String
}
