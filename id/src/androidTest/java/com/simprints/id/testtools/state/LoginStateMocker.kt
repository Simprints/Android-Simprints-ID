package com.simprints.id.testtools.state

import android.content.SharedPreferences
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.whenever
import io.reactivex.Single

object LoginStateMocker {

    fun setupLoginInfoToBeSignedIn(loginInfoManagerSpy: LoginInfoManager,
                                   projectId: String, userId: String) {

        whenever(loginInfoManagerSpy) { getSignedInProjectIdOrEmpty() } thenReturn projectId
        whenever(loginInfoManagerSpy) { getSignedInUserIdOrEmpty() } thenReturn userId
        whenever(loginInfoManagerSpy) { getEncryptedProjectSecretOrEmpty() } thenReturn projectId
        whenever(loginInfoManagerSpy) { isProjectIdSignedIn(anyNotNull()) } thenReturn true
    }

    fun setupLoginStateFullyToBeSignedIn(sharedPrefs: SharedPreferences,
                                         secureDataManagerMock: SecureDataManager,
                                         remoteDbManagerMock: RemoteDbManager,
                                         localDbManager: LocalDbManager,
                                         projectId: String,
                                         legacyApiKey: String,
                                         userId: String,
                                         projectSecret: String,
                                         localDbKey: LocalDbKey,
                                         token: String) {

        val editor = sharedPrefs.edit()
        editor.putString(LoginInfoManagerImpl.ENCRYPTED_PROJECT_SECRET, projectSecret)
        editor.putString(LoginInfoManagerImpl.PROJECT_ID, projectId)
        editor.putString(LoginInfoManagerImpl.USER_ID, userId)

        if (!legacyApiKey.isEmpty()) {
            val hashedLegacyApiKey = Hasher().hash(legacyApiKey)
            editor.putString(projectId, hashedLegacyApiKey)
            editor.putString(hashedLegacyApiKey, projectId)
        }
        editor.commit()

        whenever(secureDataManagerMock) { getLocalDbKeyOrThrow(anyNotNull()) } thenReturn localDbKey
        whenever(remoteDbManagerMock.getCurrentFirestoreToken()).thenReturn(Single.just(token))

        localDbManager.signInToLocal(secureDataManagerMock.getLocalDbKeyOrThrow(projectId))
    }
}
