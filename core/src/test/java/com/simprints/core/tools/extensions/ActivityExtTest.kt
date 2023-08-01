package com.simprints.core.tools.extensions

import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.core.domain.permission.Permission
import com.simprints.core.tools.extentions.permissionFromResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityExtTest {

    @Test
    fun `when grant result is true, then should map to Permission_Granted`() {
        val permission = runPermissionMappingTest(
            grantResult = true,
            shouldShowRationale = true
        )
        Truth.assertThat(permission).isEqualTo(Permission.Granted)
    }

    @Test
    fun `given grant result is false, when shouldShowRationale is true, then should map to Permission_Denied`() {
        val permission = runPermissionMappingTest(
            grantResult = false,
            shouldShowRationale = true
        )
        Truth.assertThat(permission).isEqualTo(Permission.Denied)
    }

    @Test
    fun `given grant result is false, when shouldShowRationale is false, then should map to Permission_DeniedNeverAskAgain`() {
        val permission = runPermissionMappingTest(
            grantResult = false,
            shouldShowRationale = false
        )
        Truth.assertThat(permission).isEqualTo(Permission.DeniedNeverAskAgain)
    }

    private fun runPermissionMappingTest(
        grantResult: Boolean,
        shouldShowRationale: Boolean
    ): Permission {
        val activity = mockk<Activity>()
        val permission = "permission"
        mockkStatic(ActivityCompat::class)
        every { ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) }
            .returns(shouldShowRationale)

        return activity.permissionFromResult(
            permission = permission,
            grantResult = grantResult
        )
    }
}
