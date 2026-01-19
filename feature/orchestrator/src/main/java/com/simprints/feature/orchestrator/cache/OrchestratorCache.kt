package com.simprints.feature.orchestrator.cache

import androidx.core.content.edit
import com.simprints.core.domain.common.AgeGroup
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.tools.OrcJsonHelper
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OrchestratorCache @Inject constructor(
    securityManager: SecurityManager,
    private val jsonHelper: OrcJsonHelper,
) {
    private val prefs = securityManager.buildEncryptedSharedPreferences(ORCHESTRATION_CACHE)

    private fun List<Step>.asJsonArray(): String = joinToString(separator = ",") {
        jsonHelper.json.encodeToString(it)
    }.let { "[$it]" }.also {
        Simber.i(tag = "orchestrator", message = "Saved steps in cache $it")
    }

    var steps: List<Step>
        set(value) {
            prefs.edit(commit = true) {
                putString(KEY_STEPS, value.asJsonArray())
            }
        }
        get() = (
            prefs
                .getString(KEY_STEPS, null)
                ?.let { jsonArray ->
                    Simber.i(tag = "orchestrator", message = "Loaded steps from cache $jsonArray")
                    jsonHelper.json.decodeFromString<List<Step>>(jsonArray)
                }
                ?: emptyList()
        )

    // This is the age group (if any) that was resolved (either coming from
    //  the Intent or from the age selection screen) for the current request
    var ageGroup: AgeGroup?
        set(value) {
            prefs.edit(commit = true) {
                putString(KEY_AGE_GROUP, value?.let { jsonHelper.json.encodeToString<AgeGroup>(it) })
            }
        }
        get() = prefs.getString(KEY_AGE_GROUP, null)?.let {
            jsonHelper.json.decodeFromString<AgeGroup>(it)
        }

    fun clearCache() {
        prefs.edit(commit = true) {
            remove(KEY_STEPS)
            remove(KEY_AGE_GROUP)
        }
    }

    companion object {
        private const val ORCHESTRATION_CACHE = "ORCHESTRATOR_CACHE"
        private const val KEY_STEPS = "steps"
        private const val KEY_AGE_GROUP = "age_group"
    }
}
