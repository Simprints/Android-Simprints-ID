package com.simprints.id.activities.setup

import android.Manifest
import com.google.common.truth.Truth
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.core.requests.SetupRequest
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality

import org.junit.Test

class PermissionsHelperTest {

    @Test
    fun `extractPermissionsFromRequest success`() {
        // Given
        val request =
            SetupRequest(listOf(Modality.FINGERPRINT, Modality.FACE), listOf(SetupPermission.LOCATION))
        // When
        val result=  PermissionsHelper.extractPermissionsFromRequest(request)
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0]).isEqualTo( Manifest.permission.ACCESS_FINE_LOCATION)
    }
    @Test
    fun `extractPermissionsFromRequest for  empty permissions`() {
        // Given
        val request =
            SetupRequest(listOf(Modality.FINGERPRINT, Modality.FACE), listOf())
        // When
        val result=  PermissionsHelper.extractPermissionsFromRequest(request)
        // Then
        Truth.assertThat(result.isEmpty()).isTrue()
    }
}
