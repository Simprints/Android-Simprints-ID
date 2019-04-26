package com.simprints.id.data.loginInfo

import com.simprints.id.data.db.ProjectIdProvider
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences

interface LoginInfoManager: ProjectIdProvider {

    var encryptedProjectSecret: String
    var signedInProjectId: String
    var signedInUserId: String
    var prefs: ImprovedSharedPreferences
    fun getEncryptedProjectSecretOrEmpty(): String

    fun getSignedInProjectIdOrEmpty(): String
    fun getSignedInUserIdOrEmpty(): String
    fun isProjectIdSignedIn(possibleProjectId: String): Boolean
    fun cleanCredentials()
    fun storeCredentials(projectId: String, userId: String)
}
