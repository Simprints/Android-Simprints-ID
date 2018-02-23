package com.simprints.id.tools.roboletric

import android.content.SharedPreferences
import com.simprints.id.Application
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.testUtils.anyNotNull
import org.mockito.Mockito
import org.mockito.stubbing.Answer

//Because Roboletric doesn't work with Realm, we need to mock the localManager to run tests with Roboeletric.
fun mockLocalDbManager(app: Application) {
    app.localDbManager = Mockito.mock(RealmDbManager::class.java)
}

fun mockRemoteDbManager(app: Application) {
    app.remoteDbManager = Mockito.mock(FirebaseManager::class.java)
}

fun mockCheckFirebaseTokenMock(app: Application, sharedPrefs: SharedPreferences) {
    mockDbManagers(app)
    val answer = Answer<Boolean> {
        sharedPrefs.getBoolean("IS_FIREBASE_TOKEN_VALID", false)
    }
    Mockito.doAnswer(answer).`when`(app.remoteDbManager).isSignedIn(anyNotNull(), anyNotNull())
}

fun mockDbManagers(app: Application) {
    mockLocalDbManager(app)
    mockRemoteDbManager(app)
}
