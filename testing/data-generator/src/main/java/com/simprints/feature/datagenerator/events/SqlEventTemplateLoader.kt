package com.simprints.feature.datagenerator.events

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Loads  SQL event files from assets/dummy_events and replaces placeholders
 * with provided values. Cached for performance.
 */
class SqlEventTemplateLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cache = ConcurrentHashMap<String, String>()

    fun getSql(
        eventName: String,
        projectId: String,
        attendantId: String,
        moduleId: String,
        scopeId: String,
    ): String {
        val template = cache.getOrPut(eventName) {
            readTemplateFromAssets(context, eventName)
        }

        return template
            .replace(PROJECT_ID_PLACEHOLDER, projectId)
            .replace(ATTENDANT_ID_PLACEHOLDER, attendantId)
            .replace(MODULE_ID_PLACEHOLDER, moduleId)
            .replace(SCOPE_ID_PLACEHOLDER, scopeId)
            .replace(SESSION_ID_PLACEHOLDER, scopeId)
            .replace(EVENT_ID_PLACEHOLDER, UUID.randomUUID().toString())
    }

    /** Reads the file from assets/dummy_events/<eventName>.sql */
    private fun readTemplateFromAssets(
        context: Context,
        eventName: String,
    ): String {
        val assetManager = context.assets
        val filePath = "$BASE_PATH/$eventName.sql"

        return try {
            assetManager.open(filePath).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load SQL file for event '$eventName': $filePath", e)
        }
    }

    fun clearCache() {
        cache.clear()
    }

    companion object {
        private const val BASE_PATH = "dummy_events"
        private const val PROJECT_ID_PLACEHOLDER = "__project_id__"
        private const val ATTENDANT_ID_PLACEHOLDER = "__attendant_id__"
        private const val MODULE_ID_PLACEHOLDER = "__module_id__"
        private const val SCOPE_ID_PLACEHOLDER = "__scope_id__"
        private const val SESSION_ID_PLACEHOLDER = "__session_id__"
        private const val EVENT_ID_PLACEHOLDER = "__event_id__"
    }
}
