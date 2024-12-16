package com.simprints.core.domain.permission

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PermissionStatusTest {
    @Test
    fun testWorstPermissionStatus_DeniedNeverAskAgain() {
        val permissions = listOf(
            PermissionStatus.Granted,
            PermissionStatus.Denied,
            PermissionStatus.DeniedNeverAskAgain,
        )
        assertThat(permissions.worstPermissionStatus()).isEqualTo(PermissionStatus.DeniedNeverAskAgain)
    }

    @Test
    fun testWorstPermissionStatus_Denied() {
        val permissions = listOf(
            PermissionStatus.Granted,
            PermissionStatus.Denied,
        )
        assertThat(permissions.worstPermissionStatus()).isEqualTo(PermissionStatus.Denied)
    }

    @Test
    fun testWorstPermissionStatus_Granted() {
        val permissions = listOf(
            PermissionStatus.Granted,
        )
        assertThat(permissions.worstPermissionStatus()).isEqualTo(PermissionStatus.Granted)
    }
}
