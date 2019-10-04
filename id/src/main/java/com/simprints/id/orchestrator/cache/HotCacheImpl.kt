package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.id.orchestrator.cache.crypto.step.StepEncoder
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.extensions.getMap
import com.simprints.id.tools.extensions.putMap
import com.simprints.id.tools.extensions.save

class HotCacheImpl(private val preferences: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    private val memoryCache
        get() = LinkedHashMap(preferences.getMap(KEY_STEPS, emptyMap()))

    override fun save(step: Step) {
        stepEncoder.encode(step).also { encodedStep ->
            saveEncodedStep(step.id, encodedStep)
        }
    }

    override fun load(): List<Step> {
        return memoryCache.values.map {
            stepEncoder.decode(it)
        }
    }

    override fun clear() {
        saveInSharedPrefs { it.putMap(KEY_STEPS, emptyMap()) }
    }

    private fun saveEncodedStep(id: String, encodedStep: String) {
        val cache = memoryCache
        cache[id] = encodedStep
        saveInSharedPrefs { it.putMap(KEY_STEPS, cache) }
    }

    private fun saveInSharedPrefs(transaction: (SharedPreferences.Editor) -> Unit) {
        with(preferences) {
            save(transaction)
        }
    }

    private companion object {
        const val KEY_STEPS = "steps"
    }
}
