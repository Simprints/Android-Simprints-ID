package com.simprints.infra.eventsync.permission

import android.content.Context
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.LastCallingPackageStore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CommCarePermissionCheckerTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var lastCallingPackageStore: LastCallingPackageStore

    private lateinit var commCarePermissionChecker: CommCarePermissionChecker

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        commCarePermissionChecker = CommCarePermissionChecker(context, lastCallingPackageStore)
    }

    @Test
    fun `hasCommCarePermissions returns true when permission is granted`() {
        // Given
        val packageName = "com.example.commcare"
        val expectedPermission = "$packageName.provider.cases.read"
        every { lastCallingPackageStore.lastCallingPackageName } returns packageName
        every { context.checkSelfPermission(expectedPermission) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = commCarePermissionChecker.hasCommCarePermissions()

        // Then
        assertThat(result).isTrue()
        verify { lastCallingPackageStore.lastCallingPackageName }
        verify { context.checkSelfPermission(expectedPermission) }
    }

    @Test
    fun `hasCommCarePermissions returns false when permission is denied`() {
        // Given
        val packageName = "com.example.commcare"
        val expectedPermission = "$packageName.provider.cases.read"
        every { lastCallingPackageStore.lastCallingPackageName } returns packageName
        every { context.checkSelfPermission(expectedPermission) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = commCarePermissionChecker.hasCommCarePermissions()

        // Then
        assertThat(result).isFalse()
        verify { lastCallingPackageStore.lastCallingPackageName }
        verify { context.checkSelfPermission(expectedPermission) }
    }

    @Test
    fun `hasCommCarePermissions handles null package name by using empty string`() {
        // Given
        val expectedPermission = ".provider.cases.read"
        every { lastCallingPackageStore.lastCallingPackageName } returns null
        every { context.checkSelfPermission(expectedPermission) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = commCarePermissionChecker.hasCommCarePermissions()

        // Then
        assertThat(result).isFalse()
        verify { lastCallingPackageStore.lastCallingPackageName }
        verify { context.checkSelfPermission(expectedPermission) }
    }

    @Test
    fun `hasCommCarePermissions handles empty package name`() {
        // Given
        val packageName = ""
        val expectedPermission = ".provider.cases.read"
        every { lastCallingPackageStore.lastCallingPackageName } returns packageName
        every { context.checkSelfPermission(expectedPermission) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = commCarePermissionChecker.hasCommCarePermissions()

        // Then
        assertThat(result).isTrue()
        verify { lastCallingPackageStore.lastCallingPackageName }
        verify { context.checkSelfPermission(expectedPermission) }
    }

    @Test
    fun `hasCommCarePermissions constructs correct permission string for different package names`() {
        // Given
        val packageName = "org.commcare.dalvik"
        val expectedPermission = "$packageName.provider.cases.read"
        every { lastCallingPackageStore.lastCallingPackageName } returns packageName
        every { context.checkSelfPermission(expectedPermission) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = commCarePermissionChecker.hasCommCarePermissions()

        // Then
        assertThat(result).isTrue()
        verify { lastCallingPackageStore.lastCallingPackageName }
        verify { context.checkSelfPermission(expectedPermission) }
    }
}
