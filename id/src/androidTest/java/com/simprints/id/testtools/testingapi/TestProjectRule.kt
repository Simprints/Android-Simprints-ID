package com.simprints.id.testtools.testingapi

import com.simprints.id.commontesttools.AndroidDefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.models.TestProjectCreationParameters
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
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
            DEFAULT_REALM_KEY)
    }

    override fun finished(description: Description?) {
        RemoteTestingManager.create().deleteTestProject(testProject.id)
    }
}
