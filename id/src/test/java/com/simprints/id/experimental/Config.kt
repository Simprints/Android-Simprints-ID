package com.simprints.id.experimental

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.simprints.id.testUtils.roboletric.TestApplication

inline fun <reified T : NewDaggerForTests> T.setupRobolectricTest() {
    app = (ApplicationProvider.getApplicationContext() as TestApplication)
    FirebaseApp.initializeApp(app)
    initComponent()
}
