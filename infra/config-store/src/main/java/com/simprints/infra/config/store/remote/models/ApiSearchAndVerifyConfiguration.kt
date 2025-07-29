package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration.Companion.DEFAULT_DOWN_SYNC_MAX_AGE
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration

@Keep
internal data class ApiSearchAndVerifyConfiguration(
    val allowedExternalCredentials: List<ApiExternalCredentialType>
)

enum class ApiExternalCredentialType {
    NHISCard, GhanaIdCard, QRCode
}

fun mapToString(a: ApiExternalCredentialType) {
    return when(a)
    {
        ApiExternalCredentialType.NHISCard -> TODO()
        ApiExternalCredentialType.GhanaIdCard -> TODO()
        ApiExternalCredentialType.QRCode -> TODO()
    }}
