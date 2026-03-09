package com.simprints.infra.aichat.engine

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the compiled knowledge base markdown from application assets.
 * This text forms the static portion of the system prompt sent to the LLM.
 */
@Singleton
internal class KnowledgeBaseLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var cached: String? = null

    fun load(): String = cached ?: context.assets
        .open(KNOWLEDGE_BASE_FILE)
        .bufferedReader()
        .use { it.readText() }
        .also { cached = it }

    companion object {
        private const val KNOWLEDGE_BASE_FILE = "knowledge_base.md"
    }
}
