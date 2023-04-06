package com.simprints.id.tools.extensions

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration


fun DownSynchronizationConfiguration.PartitionType.toGroup(): GROUP =
    when (this) {
        DownSynchronizationConfiguration.PartitionType.PROJECT -> GROUP.GLOBAL
        DownSynchronizationConfiguration.PartitionType.MODULE -> GROUP.MODULE
        DownSynchronizationConfiguration.PartitionType.USER -> GROUP.USER
    }
