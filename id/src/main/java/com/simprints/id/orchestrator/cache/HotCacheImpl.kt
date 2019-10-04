package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.id.orchestrator.cache.crypto.step.StepEncoder
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.extensions.save

class HotCacheImpl(private val preferences: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    private val memoryCache = linkedMapOf<String, String>()

    override fun save(step: Step) {
        stepEncoder.encode(step).also { encodedStep ->
            saveEncodedStep(step.id, encodedStep)
        }
    }

    private fun saveEncodedStep(id: String, encodedStep: String) {
        memoryCache[id] = encodedStep
        saveEncodedStepInSharedPrefs(id, encodedStep)

        updateStepIdsInSharedPrefsFromMemoryCache()
    }

    override fun load(): List<Step> {
        loadMemoryCacheIfNeeded()

        return memoryCache.values.map {
            stepEncoder.decode(it)
        }
    }

    private fun loadMemoryCacheIfNeeded() {
        if (memoryCache.isEmpty()) {
            loadStepIdsFromSharedPrefs()?.forEach {
                loadEncodedStepFromSharedPrefs(it)?.let { stepEncoded ->
                    memoryCache[it] = stepEncoded
                }
            }
        }
    }

    override fun clear() {
        loadStepIdsFromSharedPrefs()?.forEach {
            saveEncodedStepInSharedPrefs(it, null)
        }
        clearStepIdsInSharedPrefs()
        memoryCache.clear()
    }

    /**
     * load/save steps ids from shared prefs
     */
    private fun loadStepIdsFromSharedPrefs() =
        preferences.getStringSet(KEY_STEPS, setOf())


    private fun clearStepIdsInSharedPrefs() {
        saveInSharedPrefs { it.putStringSet(KEY_STEPS, setOf()) }
    }

    private fun updateStepIdsInSharedPrefsFromMemoryCache() {
        saveInSharedPrefs { it.putStringSet(KEY_STEPS, memoryCache.keys) }
    }


    /**
     * load/save steps from shared prefs
     */
    private fun loadEncodedStepFromSharedPrefs(stepId: String) =
        preferences.getString("${KEY_STEPS}_$stepId", null)


    private fun saveEncodedStepInSharedPrefs(stepId: String, encodedStep: String?) {
        saveInSharedPrefs { it.putString("${KEY_STEPS}_$stepId", encodedStep) }
    }


    private fun saveInSharedPrefs(transaction: (SharedPreferences.Editor) -> SharedPreferences.Editor) {
        with(preferences) {
            save(transaction)
        }
    }

    private companion object {
        const val KEY_STEPS = "steps"
    }
}
