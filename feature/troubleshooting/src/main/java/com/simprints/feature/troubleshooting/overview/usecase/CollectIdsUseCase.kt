package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.core.DeviceID
import com.simprints.infra.authstore.AuthStore
import javax.inject.Inject

internal class CollectIdsUseCase @Inject constructor(
    @DeviceID private val deviceID: String,
    private val authStore: AuthStore,
) {
    operator fun invoke() =
        """
        Device ID: $deviceID
        Project ID: ${authStore.signedInProjectId}
        User ID: ${authStore.signedInUserId?.value.orEmpty()}
        """.trimIndent()
}
