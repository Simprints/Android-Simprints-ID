package com.simprints.id.commontesttools.state

import android.content.SharedPreferences
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import io.mockk.coEvery

object LoginStateMocker {

    fun setupLoginStateFullyToBeSignedIn(sharedPrefs: SharedPreferences,
                                         secureLocalDbKeyProviderMock: SecureLocalDbKeyProvider,
                                         remoteDbManagerMock: RemoteDbManager,
                                         projectId: String,
                                         userId: String,
                                         projectSecret: String,
                                         localDbKey: LocalDbKey,
                                         token: String) {

        val editor = sharedPrefs.edit()
        editor.putString(LoginInfoManagerImpl.ENCRYPTED_PROJECT_SECRET, projectSecret)
        editor.putString(LoginInfoManagerImpl.PROJECT_ID, projectId)
        editor.putString(LoginInfoManagerImpl.USER_ID, userId)
        editor.commit()

        whenever(secureLocalDbKeyProviderMock) { getLocalDbKeyOrThrow(anyNotNull()) } thenReturn localDbKey
        coEvery { remoteDbManagerMock.getCurrentToken() } returns token
    }
}
