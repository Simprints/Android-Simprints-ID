package com.simprints.infra.aichat.engine

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the compiled knowledge base and internal reference documents from
 * application assets. This text forms the static portion of the system
 * prompt sent to the LLM.
 */
@Singleton
internal class KnowledgeBaseLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var cached: String? = null

    fun load(): String = cached ?: buildString {
        append(loadAsset(KNOWLEDGE_BASE_FILE))
        appendLine()
        appendLine()
        append(loadAsset(INTERNAL_REFERENCE_FILE))
    }.also { cached = it }

    private fun loadAsset(filename: String): String = context.assets
        .open(filename)
        .bufferedReader()
        .use { it.readText() }

    companion object {
        private const val KNOWLEDGE_BASE_FILE = "knowledge_base.md"
        private const val INTERNAL_REFERENCE_FILE = "internal_reference.md"
    }
}
