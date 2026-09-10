package com.simprints.feature.moduleselector.adapter

import com.simprints.core.domain.tokenization.TokenizableString

internal sealed interface ModuleSelectorItem {
    data class Module(
        val name: String,
        val tokenizedName: TokenizableString,
        val isSelected: Boolean,
    ) : ModuleSelectorItem

    data object NoResult : ModuleSelectorItem
}
