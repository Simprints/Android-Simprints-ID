package com.simprints.infra.aichat.engine

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Selects the best available [ChatEngine] based on current device capabilities
 * and connectivity. Priority order:
 * 1. Cloud (Firebase AI) — if online
 * 2. Offline FAQ — always available as last resort
 */
@Singleton
internal class ChatEngineSelector @Inject constructor(
    private val cloudEngine: FirebaseChatEngine,
    private val offlineEngine: OfflineFaqEngine,
) {
    suspend fun selectEngine(): ChatEngine = when {
        cloudEngine.isAvailable() -> cloudEngine
        else -> offlineEngine
    }
}
