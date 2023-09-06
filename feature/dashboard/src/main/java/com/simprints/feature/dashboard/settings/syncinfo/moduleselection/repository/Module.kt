package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.simprints.core.domain.tokenization.TokenizedString

internal data class Module(val name: TokenizedString, var isSelected: Boolean)
