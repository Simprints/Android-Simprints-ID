package com.simprints.feature.orchestrator.cache

import android.os.Bundle
import androidx.core.content.edit
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.orchestrator.steps.SerializableMixin
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.infra.security.SecurityManager
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OrchestratorCache @Inject constructor(
    securityManager: SecurityManager,
    private val jsonHelper: JsonHelper,
) {

    private val prefs = securityManager.buildEncryptedSharedPreferences(ORCHESTRATION_CACHE)
    private fun List<Step>.asJsonArray(jsonHelper: JsonHelper): String = with(jsonHelper) {
        addMixin(Serializable::class.java, SerializableMixin::class.java)
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
        get() = prefs.getString(KEY_STEPS, null)
            ?.let { jsonArray ->
                jsonHelper.addMixin(Serializable::class.java, SerializableMixin::class.java)
                jsonHelper.fromJson(
                    json = jsonArray,
                    module = stepsModule,
                    type = object : TypeReference<List<Step>>() {},
                )
            }
            ?: emptyList()

    fun clearSteps() {
        prefs.edit(commit = true) {
            remove(KEY_STEPS)
        }
    }

    companion object {
        private const val ORCHESTRATION_CACHE = "ORCHESTRATOR_CACHE"
        private const val KEY_STEPS = "steps"

        private val stepsModule = SimpleModule().apply {
            addSerializer(Bundle::class.java, BundleSerializer())
            addDeserializer(Bundle::class.java, BundleDeserializer())
        }
    }
}
