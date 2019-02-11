package com.simprints.id.testtools

import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.testtools.models.TestProject
import com.simprints.id.testtools.models.TestProjectCreationParameters
import com.simprints.id.testtools.remote.RemoteTestingManager
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Creates a new [TestProject] on the backend.
 * Usage as a [TestRule]:
 * @get:Rule val testProjectRule = TestProjectRule()
 */
class TestProjectRule(
    val testProjectCreationParameters: TestProjectCreationParameters = TestProjectCreationParameters()
) : TestWatcher() {

    lateinit var testProject: TestProject
    lateinit var localDbKey: LocalDbKey

    override fun starting(description: Description?) {

        testProject = RemoteTestingManager.create().createTestProject(testProjectCreationParameters)

        localDbKey = LocalDbKey(
            testProject.id,
            DefaultTestConstants.DEFAULT_REALM_KEY,
            testProject.legacyId)
    }

    override fun finished(description: Description?) {
        RemoteTestingManager.create().deleteTestProject(testProject.id)
        StorageUtils.clearRealmDatabase(PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId))
    }
}
