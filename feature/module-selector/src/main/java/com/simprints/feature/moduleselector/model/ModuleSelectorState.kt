package com.simprints.feature.moduleselector.model

import com.simprints.feature.moduleselector.arch.State
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.eventsync.module.SelectableModule

internal data class ModuleSelectorState(
    val maxNumberOfModules: Int = 0,
    val allModules: List<SelectableModule> = emptyList(),
    val project: Project? = null,
    val settingsPassword: SettingsPasswordConfig = SettingsPasswordConfig.NotSet,
    val query: String = "",
    val onlySelected: Boolean = false,
) : State
