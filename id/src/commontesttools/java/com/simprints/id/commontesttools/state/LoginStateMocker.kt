package com.simprints.id.commontesttools.state

import android.content.SharedPreferences
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Single

// Duplicated from id/commontestttools

object LoginStateMocker {

    fun setupLoginStateFullyToBeSignedIn(sharedPrefs: SharedPreferences,
                                         secureDataManagerMock: SecureDataManager,
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

        whenever(secureDataManagerMock) { getLocalDbKeyOrThrow(anyNotNull()) } thenReturn localDbKey
        whenever(remoteDbManagerMock.getCurrentToken()).thenReturn(Single.just(token))
    }
}
