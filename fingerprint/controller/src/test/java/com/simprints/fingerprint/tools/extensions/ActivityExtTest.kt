package com.simprints.fingerprint.tools.extensions

import android.app.Activity
import android.content.Context
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.permission.Permission
import com.simprints.core.tools.extentions.permissionFromResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
class ActivityExtTest {

    @Before
    fun setUp() {
        mockkStatic(Looper::class)
    }

    @Test
    fun `test runOnUiThread if already in mainthread`() {
        // Given
        every { Looper.myLooper() } returns Looper.getMainLooper()
        // When
        var x = 1
        val context = spyk<Context>()
        context.runOnUiThread {
            x = 2
        }
        // Then
        assertThat(x).isEqualTo(2)
    }

    @Test
    fun `test runOnUiThread from another thread`() {
        // Given
        every { Looper.myLooper() } returns mockk(relaxed = true)

        // When
        var x = 1
        val context = spyk<Context>()
        context.runOnUiThread {
            x = 2
        }
        ShadowLooper.idleMainLooper()
        // Then
        assertThat(x).isEqualTo(2)
    }

    @Test
    fun `when grant result is true, then should map to Permission_Granted`() {
        val permission = runPermissionMappingTest(
            grantResult = true,
            shouldShowRationale = true
        )
        assertThat(permission).isEqualTo(Permission.Granted)
    }

    @Test
    fun `given grant result is false, when shouldShowRationale is true, then should map to Permission_Denied`() {
        val permission = runPermissionMappingTest(
            grantResult = false,
            shouldShowRationale = true
        )
        assertThat(permission).isEqualTo(Permission.Denied)
    }

    @Test
    fun `given grant result is false, when shouldShowRationale is false, then should map to Permission_DeniedNeverAskAgain`() {
        val permission = runPermissionMappingTest(
            grantResult = false,
            shouldShowRationale = false
        )
        assertThat(permission).isEqualTo(Permission.DeniedNeverAskAgain)
    }

    private fun runPermissionMappingTest(
        grantResult: Boolean,
        shouldShowRationale: Boolean
    ): Permission {
        val activity = mockk<Activity>()
        val permission = "permission"
        mockk<ActivityCompat>()
        every { ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) }
            .returns(shouldShowRationale)

        return activity.permissionFromResult(
            permission = permission,
            grantResult = grantResult
        )
    }
}
