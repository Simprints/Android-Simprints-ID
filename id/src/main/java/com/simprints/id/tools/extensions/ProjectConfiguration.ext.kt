package com.simprints.id.tools.extensions

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration

fun GeneralConfiguration.Modality.toMode(): Modes =
    when (this) {
        GeneralConfiguration.Modality.FACE -> Modes.FACE
        GeneralConfiguration.Modality.FINGERPRINT -> Modes.FINGERPRINT
    }

fun DownSynchronizationConfiguration.PartitionType.toGroup(): GROUP =
    when (this) {
        DownSynchronizationConfiguration.PartitionType.PROJECT -> GROUP.GLOBAL
        DownSynchronizationConfiguration.PartitionType.MODULE -> GROUP.MODULE
        DownSynchronizationConfiguration.PartitionType.USER -> GROUP.USER
    }
