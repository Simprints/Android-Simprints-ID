package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.simprints.core.domain.tokenization.TokenizableString

internal data class Module(
    val name: TokenizableString,
    var isSelected: Boolean,
)
