package com.simprints.infra.eventsync.module

import com.simprints.core.domain.tokenization.TokenizableString

data class SelectableModule(
    val name: TokenizableString,
    var isSelected: Boolean,
)
