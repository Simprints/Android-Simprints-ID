package com.simprints.id.testTemplates

import android.support.test.InstrumentationRegistry
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.testTemplates.FirstUseLocal.Companion.realmKey
import com.simprints.id.testTools.models.TestProject
import com.simprints.id.testTools.remote.RemoteTestingManager
import io.realm.Realm
import io.realm.RealmConfiguration

interface FirstUse : FirstUseLocal {

    var testProject: TestProject

    override var peopleRealmConfiguration: RealmConfiguration

    override fun setUp() {

        testProject = RemoteTestingManager.create().createTestProject()

        val localDbKey = LocalDbKey(
            testProject.id,
            realmKey,
            testProject.legacyId)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

        super.setUp()
    }

    override fun tearDown() {
        RemoteTestingManager.create().deleteTestProject(testProject.id)
        super.tearDown()
    }
}
