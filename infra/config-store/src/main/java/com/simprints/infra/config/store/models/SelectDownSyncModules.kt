package com.simprints.infra.config.store.models

import com.simprints.core.domain.tokenization.TokenizableString

data class SelectDownSyncModules(
    val id: String,
    val moduleIds: List<TokenizableString>,
)
