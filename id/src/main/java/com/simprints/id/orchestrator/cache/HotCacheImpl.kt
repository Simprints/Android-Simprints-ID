package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.id.orchestrator.cache.crypto.StepEncoder
import com.simprints.id.orchestrator.steps.Step

class HotCacheImpl(private val preferences: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    private val steps = preferences.getStringSet(KEY_STEPS, null)?.toMutableSet()

    override fun save(step: Step?) {
        step?.let {
            stepEncoder.encode(it).also { encodedStep ->
                steps?.add(encodedStep)
                preferences.edit()?.putStringSet(KEY_STEPS, steps)?.apply()
            }
        }
    }

    override fun load() = steps?.mapNotNull(stepEncoder::decode)

    override fun clear() {
        preferences.edit().putStringSet(KEY_STEPS, null).apply()
    }

    private companion object {
        const val KEY_STEPS = "steps"
    }

}
