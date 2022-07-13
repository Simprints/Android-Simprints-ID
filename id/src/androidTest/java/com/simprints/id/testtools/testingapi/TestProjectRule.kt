package com.simprints.id.testtools.testingapi

import android.content.Context
import com.simprints.core.security.LocalDbKey
import com.simprints.id.commontesttools.AndroidDefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.models.TestProjectCreationParameters
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Creates a new [TestProject] on the backend.
 * Usage as a [TestRule]:
 * @build:Rule val testProjectRule = TestProjectRule()
 */
class TestProjectRule(
    private val ctx: Context,
    private val testProjectCreationParameters: TestProjectCreationParameters = TestProjectCreationParameters()
) : TestWatcher() {

    lateinit var testProject: TestProject
    lateinit var localDbKey: LocalDbKey

    override fun starting(description: Description?) = runBlocking {

        testProject =
            RemoteTestingManager.create(ctx).createTestProject(testProjectCreationParameters)

        localDbKey = LocalDbKey(
            testProject.id,
            DEFAULT_REALM_KEY
        )
    }

    override fun finished(description: Description?) = runBlocking {
        RemoteTestingManager.create(ctx).deleteTestProject(testProject.id)
    }
}
