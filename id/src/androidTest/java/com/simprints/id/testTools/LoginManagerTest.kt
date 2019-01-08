package com.simprints.id.testTools

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.doReturn
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import io.reactivex.Single

class LoginManagerTest {

    fun setUpSignedInState(sharedPrefs: SharedPreferences,
                           secureDataManagerMock: SecureDataManager,
                           remoteDbManagerMock: RemoteDbManager,
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

        doReturn(localDbKey).`when`(secureDataManagerMock).getLocalDbKeyOrThrow(anyNotNull())
        whenever(remoteDbManagerMock.getCurrentFirestoreToken()).thenReturn(Single.just(token))

    }
}
