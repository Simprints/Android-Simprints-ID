package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface SecureDataManager {

    var projectKey: String
    var prefs: ImprovedSharedPreferences
    fun getProjectKeyOrEmpty(): String

    /*TODO: Legacy stuff to refactor */
    fun getApiKeyOr(default: String): String
    var apiKey: String
}
