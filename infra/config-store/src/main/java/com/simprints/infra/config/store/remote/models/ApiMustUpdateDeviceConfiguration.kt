package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.SelectDownSyncModules
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiMustUpdateDeviceConfiguration(
    val id: String,
    val configuration: ApiDeviceConfigurationUpdate,
) {
    fun fromApiToDomain(): SelectDownSyncModules = SelectDownSyncModules(
        id = id,
        moduleIds = configuration.downSyncModules.map(String::asTokenizableEncrypted),
    )

    @Keep
    @Serializable
    internal data class ApiDeviceConfigurationUpdate(
        val downSyncModules: List<String>,
    )
}
