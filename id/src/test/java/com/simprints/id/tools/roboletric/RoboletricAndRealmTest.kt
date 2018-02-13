package com.simprints.id.tools.roboletric

import com.simprints.id.Application
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import org.mockito.Mockito

//Because Roboletric doesn't work with Realm, we need to mock the localManager to run tests with Roboeletric.
fun mockLocalDbManager(app: Application) {
    app.localDbManager = Mockito.mock(RealmDbManager::class.java)
}

fun mockRemoteDbManager(app: Application) {
    app.remoteDbManager = Mockito.mock(FirebaseManager::class.java)
}

fun mockDbManagers(app: Application) {
    mockLocalDbManager(app)
    mockRemoteDbManager(app)
}
