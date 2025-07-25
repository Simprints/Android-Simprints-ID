package com.simprints.feature.orchestrator.cache

import androidx.core.content.edit
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepParamsMixin
import com.simprints.feature.orchestrator.steps.StepResultMixin
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OrchestratorCache @Inject constructor(
    securityManager: SecurityManager,
    private val jsonHelper: JsonHelper,
) {
    private val prefs = securityManager.buildEncryptedSharedPreferences(ORCHESTRATION_CACHE)

    private fun List<Step>.asJsonArray(jsonHelper: JsonHelper): String = with(jsonHelper) {
        addMixin(StepParams::class.java, StepParamsMixin::class.java)
        addMixin(StepResult::class.java, StepResultMixin::class.java)
        return@with joinToString(separator = ",") {
            jsonHelper.toJson(it, module = stepsModule)
        }.let { "[$it]" }
    }

    var steps: List<Step>
        set(value) {
            prefs.edit(commit = true) {
                putString(KEY_STEPS, value.asJsonArray(jsonHelper))
            }
        }
        get() = prefs
            .getString(KEY_STEPS, null)
            ?.let { jsonArray ->
                jsonHelper.addMixin(StepParams::class.java, StepParamsMixin::class.java)
                jsonHelper.addMixin(StepResult::class.java, StepResultMixin::class.java)
                jsonHelper.fromJson(
                    json = jsonArray,
                    module = stepsModule,
                    type = object : TypeReference<List<Step>>() {},
                )
            }
            ?: emptyList()

    // This is the age group (if any) that was resolved (either coming from
    //  the Intent or from the age selection screen) for the current request
    var ageGroup: AgeGroup?
        set(value) {
            prefs.edit(commit = true) {
                putString(KEY_AGE_GROUP, value?.let { jsonHelper.toJson(it) })
            }
        }
        get() = prefs.getString(KEY_AGE_GROUP, null)?.let { jsonHelper.fromJson(it, object : TypeReference<AgeGroup>() {}) }

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

        private val stepsModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}
