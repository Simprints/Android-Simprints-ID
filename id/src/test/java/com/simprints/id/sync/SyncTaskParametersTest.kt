package com.simprints.id.sync

import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.domain.Constants
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncTaskParametersTest {

    @Test
    fun buildSyncTaskParametersTest() {
        val userSync = SyncTaskParameters.UserSyncTaskParameters("projectId", "userId")
        val moduleSync = SyncTaskParameters.ModuleIdSyncTaskParameters("projectId", "moduleId")
        val projectSync = SyncTaskParameters.GlobalSyncTaskParameters("projectId")

        Assert.assertNotNull(userSync.projectId)
        Assert.assertNotNull(userSync.userId)
        Assert.assertEquals(userSync.toGroup(), Constants.GROUP.USER)
        Assert.assertEquals(userSync.toMap(), hashMapOf("projectId" to "projectId", "userId" to "userId"))

        Assert.assertNotNull(moduleSync.projectId)
        Assert.assertNotNull(moduleSync.moduleId)
        Assert.assertEquals(moduleSync.toGroup(), Constants.GROUP.MODULE)
        Assert.assertEquals(moduleSync.toMap(), hashMapOf("projectId" to "projectId", "moduleId" to "moduleId"))

        Assert.assertNotNull(projectSync.projectId)
        Assert.assertEquals(projectSync.toGroup(), Constants.GROUP.GLOBAL)
        Assert.assertEquals(projectSync.toMap(), hashMapOf("projectId" to "projectId"))
    }
}
