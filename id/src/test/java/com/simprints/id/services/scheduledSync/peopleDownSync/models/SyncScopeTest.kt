package com.simprints.id.services.scheduledSync.peopleDownSync.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.syncscope.domain.DownSyncScope
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncScopeTest {

    @Test
    fun syncScopeUniqueKey(){
        val syncScope = DownSyncScope("projectId", "userId", setOf("moduleId"))
        assertThat(syncScope.uniqueKey).isEqualTo("projectId_userId_moduleId")
    }

    @Test
    fun syncScopeUniqueKeyWithNoUser(){
        val syncScope = DownSyncScope("projectId", null, setOf("moduleId"))
        assertThat(syncScope.uniqueKey).isEqualTo("projectId__moduleId")
    }

    @Test
    fun syncScopeUniqueKeyWithNoModule(){
        val syncScope = DownSyncScope("projectId", "userId", null)
        assertThat(syncScope.uniqueKey).isEqualTo("projectId_userId_")
    }

    @Test
    fun syncScopeUniqueKeyWithMultipleModules(){
        val syncScope = DownSyncScope("projectId", "userId", setOf("module1", "module2"))
        assertThat(syncScope.uniqueKey).isEqualTo("projectId_userId_module1_module2")
    }

    @Test
    fun syncScopeGeneratesSubScopes(){
        val syncScope = DownSyncScope("projectId", "userId", setOf("module1", "module2"))
        assertThat(syncScope.toSubSyncScopes().first()).isEqualTo(SubSyncScope("projectId", "userId", "module1"))
        assertThat(syncScope.toSubSyncScopes().last()).isEqualTo(SubSyncScope("projectId", "userId", "module2"))
    }

    @Test
    fun syncScopeGeneratesSubScopesWithNoModules(){
        val syncScope = DownSyncScope("projectId", "userId", null)
        assertThat(syncScope.toSubSyncScopes().first()).isEqualTo(SubSyncScope("projectId", "userId", null))
    }

    @Test
    fun syncScopeGeneratesEmptySubScopesWithEmptyModuleList() {
        val syncScope = DownSyncScope("projectId", null, setOf())
        Assert.assertTrue(syncScope.toSubSyncScopes().isEmpty())
    }
}
